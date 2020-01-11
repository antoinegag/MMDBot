package com.mcmoddev.bot.events.users;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.misc.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public final class EventRoleAdded extends ListenerAdapter {

	/**
	 *
	 */
    @Override
    public void onGuildMemberRoleAdd(final GuildMemberRoleAddEvent event) {
    	final User user = event.getUser();
        final EmbedBuilder embed = new EmbedBuilder();
        final Guild guild = event.getGuild();
        final Long guildId = guild.getIdLong();
        final TextChannel channel = guild.getTextChannelById(MMDBot.getConfig().getChannelIDImportantEvents());

        Utils.sleepTimer();

        final AuditLogPaginationAction paginationAction = event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_ROLE_UPDATE);
        paginationAction.complete();

        final AuditLogEntry entry = paginationAction.getLast();
        final User editor = entry.getUser();

        String editorID = "Unknown";
        String editorTag = "Unknown";
        if (editor != null) {
            editorID = editor.getId();
            editorTag = editor.getAsTag();
        }

        final List<Role> currentRoles = new ArrayList<>(event.getMember().getRoles());
        final List<Role> addedRoles = new ArrayList<>(event.getRoles());

        if (MMDBot.getConfig().getGuildID().equals(guildId)) {
            embed.setColor(Color.YELLOW);
            embed.setTitle("User Role Added");
            embed.setThumbnail(user.getEffectiveAvatarUrl());
            embed.addField("User:", user.getAsTag(), true);
            embed.addField("User ID:", user.getId(), true);

            embed.addField("Edited By:", editorTag, true);
            embed.addField("Editor ID:", editorID, true);
            embed.addField("Current Roles:", currentRoles.stream().map(IMentionable::getAsMention).collect(Collectors.joining()), false);
            embed.addField("Added Roles:", addedRoles.stream().map(IMentionable::getAsMention).collect(Collectors.joining()), false);
            embed.setTimestamp(Instant.now());
            channel.sendMessage(embed.build()).queue();
        }
    }
}
