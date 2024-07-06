/*
 * Copyright (c) 2024. By Jan-Michael Sohn also known as @xxAROX.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xxAROX.PresenceMan.PowerNukkitX.tasks.async;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import xxAROX.PresenceMan.PowerNukkitX.PresenceMan;
import xxAROX.PresenceMan.PowerNukkitX.entity.Gateway;
import xxAROX.PresenceMan.PowerNukkitX.tasks.ReconnectingTask;
import xxAROX.WebRequester.WebRequester;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public final class FetchGatewayInformationTask extends AsyncTask {
    public static final String URL = "https://raw.githubusercontent.com/Presence-Man/Gateway/main/gateway.json";

    @Override
    public void onRun() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Cache-Control", "no-cache, no-store");
        try {
            CompletableFuture<WebRequester.Result> future = WebRequester.getAsync(URL, headers);
            WebRequester.Result response = future.get();
            try {
                if (response == null || response.getStatus() != 200) PresenceMan.getInstance().getLogger().warning("Couldn't fetch gateway data");
                else {
                    JsonObject json = PresenceMan.GSON.fromJson(response.getBody(), JsonObject.class);
                    if (json != null) {
                        Gateway.protocol = json.get("protocol").getAsString();
                        Gateway.address = json.get("address").getAsString();
                        Gateway.port = json.has("port") && !json.get("port").isJsonNull() ? json.get("port").getAsInt() : null;
                        ping_backend(success -> {
                            if (!success) PresenceMan.getInstance().getLogger().error("Error while connecting to backend-server!");
                        });
                    }
                }
            } catch (JsonParseException e) {
                PresenceMan.getInstance().getLogger().error("Error while fetching gateway information: " + e.getMessage());
            }
        } catch (ExecutionException | InterruptedException e) {
            PresenceMan.getInstance().getLogger().critical("Presence-Man backend-gateway config is not reachable, disabling..");
            PresenceMan.getInstance().setEnabled(false);
            return;
        }
    }

    public static void ping_backend(Consumer<Boolean> callback) {
        if (ReconnectingTask.active) return;

        PresenceMan.getInstance().getServer().getScheduler().scheduleAsyncTask(PresenceMan.getInstance(), new AsyncTask() {
            @Override
            public void onRun() {
                try {
                    CompletableFuture<WebRequester.Result> future = WebRequester.getAsync(Gateway.getUrl());
                    WebRequester.Result response = future.get();
                    setResult(response.getStatus());
                } catch (ExecutionException | InterruptedException e) {
                    setResult(404);
                }
            }

            @Override
            public void onCompletion(Server server) {
                int code = (int) getResult();
                if (code != 200) {
                    Gateway.broken = true;
                    ReconnectingTask.activate();
                } else {
                    ReconnectingTask.deactivate();
                    PresenceMan.getInstance().getLogger().notice("This server will be displayed as '" + PresenceMan.server + "' in presences!");
                }
                callback.accept(code == 200);
            }
        });
    }

    public static void unga_bunga() {
        Server.getInstance().getScheduler().scheduleAsyncTask(PresenceMan.getInstance(), new FetchGatewayInformationTask());
    }
}
