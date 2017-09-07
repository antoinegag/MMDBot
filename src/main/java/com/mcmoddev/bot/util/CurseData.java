package com.mcmoddev.bot.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Based on code by Jared
public class CurseData {

    // TODO clean this all up it's gross lol
    // TODO add a time stamp to regenerate
    // TODO implemnt caching
    public static final String PROFILE_URL = "https://minecraft.curseforge.com/members/";

    public static final String PROJECTS_PAGE_URL = "https://minecraft.curseforge.com/members/%s/projects?page=";

    public static final String PROJECT_URL = "https://minecraft.curseforge.com/";

    private String username;

    private String avatar;

    private String profile;

    private List<String> projectURLs;

    private Map<String, Long> downloads;

    private long totalDownloads;

    private boolean foundUser = true;

    private boolean foundProject = false;

    public CurseData (String username) {

        this.username = username;
        this.profile = PROFILE_URL + username;
        this.projectURLs = new ArrayList<>();
        this.downloads = new HashMap<>();

        final String baseURL = String.format(PROJECTS_PAGE_URL, username);

        int page = 1;
        boolean foundDuplicatePage = false;
        final boolean foundAvatar = false;

        while (!foundDuplicatePage) {

            try (InputStream stream = new URL(baseURL + page).openStream()) {

                final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                for (String line = reader.readLine(); line != null; line = reader.readLine())

                    // Find projects
                    if (line.contains("a href=\"/projects/")) {

                        final String projectURL = "https://minecraft.curseforge.com" + line.split("\"")[1].split("\"")[0];

                        if (!this.projectURLs.contains(projectURL)) {

                            this.projectURLs.add(projectURL);
                            this.foundProject = true;
                        }

                        else {

                            foundDuplicatePage = true;
                            break;
                        }
                    }

                if (page == 1 && !this.foundProject)
                    return;
            }

            catch (final Exception e) {

                this.foundUser = false;
                break;
            }

            page++;
        }

        if (!this.foundUser || !this.foundProject)
            return;

        // downloads
        for (final String projectUrl : this.projectURLs)
            try (InputStream stream = new URL(projectUrl).openStream()) {

                final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                boolean foundDownloads = false;

                for (String line = reader.readLine(); line != null; line = reader.readLine())
                    if (foundDownloads) {

                        final long projectDownloads = Long.parseLong(line.split(">")[1].split("<")[0].replaceAll(",", ""));
                        this.totalDownloads += projectDownloads;
                        final String name = projectUrl.replace("https://minecraft.curseforge.com/projects/", "").replaceAll("-", " ");

                        this.downloads.put("[" + name + "](" + projectUrl.replaceAll(" ", "-") + ")", projectDownloads);
                        break;
                    }

                    else if (line.contains("Total Downloads"))
                        foundDownloads = true;
            }

            catch (final Exception e) {

                e.printStackTrace();
            }

        this.downloads = Utilities.sortByValue(this.downloads, true);
    }

    public boolean exists () {

        return this.foundUser;
    }

    public String getUsername () {

        return this.username;
    }

    public String getAvatar () {

        return this.avatar;
    }

    public boolean hasAvatar () {

        return this.avatar != null && !this.avatar.isEmpty();
    }

    public String getProfile () {

        return this.profile;
    }

    public List<String> getProjectURLs () {

        return this.projectURLs;
    }

    public Map<String, Long> getDownloads () {

        return this.downloads;
    }

    public boolean hasProjects () {

        return this.foundProject && !this.projectURLs.isEmpty() && !this.downloads.isEmpty();
    }

    public int getProjectCount () {

        return this.projectURLs.size();
    }

    public long getTotalDownloads () {

        return this.totalDownloads;
    }
}
