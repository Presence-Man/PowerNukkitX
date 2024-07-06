/*
 * Copyright (c) Jan-Michael Sohn
 * All rights reserved.
 * Only people with the explicit permission from Jan Sohn are allowed to modify, share or distribute this code.
 */

package xxAROX.PresenceMan.PowerNukkitX.tasks.async;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.NotNull;
import xxAROX.PresenceMan.PowerNukkitX.PresenceMan;
import xxAROX.PresenceMan.PowerNukkitX.entity.Gateway;
import xxAROX.PresenceMan.PowerNukkitX.tasks.ReconnectingTask;
import xxAROX.PresenceMan.PowerNukkitX.utils.Utils;
import xxAROX.WebRequester.WebRequester;

import java.io.IOException;
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
