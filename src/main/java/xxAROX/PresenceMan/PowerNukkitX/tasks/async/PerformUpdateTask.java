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
import xxAROX.PresenceMan.PowerNukkitX.PresenceMan;
import xxAROX.PresenceMan.PowerNukkitX.entity.Gateway;
import xxAROX.PresenceMan.PowerNukkitX.tasks.UpdateCheckerTask;
import xxAROX.PresenceMan.PowerNukkitX.utils.Utils;
import xxAROX.WebRequester.WebRequester;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PerformUpdateTask extends AsyncTask {
    private static final String LATEST_VERSION_URL = "https://raw.githubusercontent.com/Presence-Man/PowerNukkitX/main/latest_version.txt";

    private final String currentVersion;
    private boolean notified = false;

    public PerformUpdateTask() {
        currentVersion = PresenceMan.getInstance().getDescription().getVersion();
    }

    @Override
    public void onRun() {
        try {
            CompletableFuture<WebRequester.Result> future = WebRequester.getAsync(LATEST_VERSION_URL);
            WebRequester.Result response = future.get();
            if (response.getStatus() != 200 || response.getBody() == null || response.getBody().isEmpty()) setResult(null);
            else {
                Pattern versionPattern = Pattern.compile("\\d+(\\.\\d+)*");
                Matcher matcher = versionPattern.matcher(response.getBody().trim());

                if (matcher.find()) {
                    String latestVersionString = matcher.group();
                    Utils.VersionComparison.Version latest = Utils.VersionComparison.parse(latestVersionString);
                    Utils.VersionComparison.Version current = Utils.VersionComparison.parse(currentVersion);

                    setResult(latest.compareTo(current) < 0 ? latestVersionString : null);
                } else setResult(null);
            }
        } catch (InterruptedException | ExecutionException e) {
            PresenceMan.getInstance().getLogger().error("" + e);
            setResult(null);
        }
    }

    @Override
    public void onCompletion(Server server) {
        String latest = (String) getResult();
        if (latest != null) {
            if (!notified) {
                PresenceMan.getInstance().getLogger().warning("Your version of Presence-Man is out of date. To avoid issues please update it to the latest version!");
                PresenceMan.getInstance().getLogger().warning("Download: " + Gateway.getUrl() + "/downloads/powernukkitx");
                notified = true;
            }
        }
        UpdateCheckerTask.running = false;
    }
}
