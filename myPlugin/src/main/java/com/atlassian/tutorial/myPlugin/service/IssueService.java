package com.atlassian.tutorial.myPlugin.service;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.thread.JiraThreadLocalUtils;
import com.atlassian.jira.web.bean.PagerFilter;

import java.util.List;
import java.util.stream.Collectors;

public class IssueService {

    public static String getOpenIssuesAsText() {
        JiraThreadLocalUtils.preCall();
        try {
            SearchService searchService = ComponentAccessor.getComponent(SearchService.class);
            ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
            if (user == null) {
                user = ComponentAccessor.getUserManager().getUserByName("admin");
            }

            if (user == null) {
                return "Пользователь не авторизован.";
            }

            String jql = "statusCategory != Done ORDER BY created DESC";
            SearchService.ParseResult parseResult = searchService.parseQuery(user, jql);

            if (!parseResult.isValid()) {
                return "Ошибка JQL: " + parseResult.getErrors();
            }

            List<Issue> issues = searchService.search(user, parseResult.getQuery(), PagerFilter.getUnlimitedFilter()).getResults();

            if (issues.isEmpty()) {
                return "Нет открытых задач.";
            }

            return issues.stream()
                    .limit(10)
                    .map(issue -> issue.getKey() + ": " + issue.getSummary())
                    .collect(Collectors.joining("\n"));

        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка при поиске задач.";
        } finally {
            JiraThreadLocalUtils.postCall(); // Важно!
        }
    }
}
