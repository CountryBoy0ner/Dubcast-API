# Project Overview

This section covers the business context, goals, and requirements for the **Dubcast** diploma project.

## Contents

- [Problem Statement & Goals](problem-and-goals.md)
- [Stakeholders & Users](stakeholders.md)
- [Scope](scope.md)
- [Features](features.md)
---

## Executive Summary

Dubcast is an internet radio focused primarily on the web, where all listeners use the same global timeline: when you join, playback starts at the correct timestamp of the currently scheduled track. The project is aimed at small communities (for example, student groups) who want to have a live, human-curated listening experience, rather than generated playlists. And in general, for anyone who likes to listen to high-quality music. The system combines the Spring Boot backend (schedule, playback status, SoundCloud parsing/import, REST API) with a simple web interface that shows "Currently Playing", online listeners, and chat/user profiles. As a result, Dubcast provides a reproducible Docker-based setup, a documented API (OpenAPI/Swagger), and a CI/CD pipeline for verifying changes and publishing container images.

---

## Key Highlights

| Aspect | Description |
|---|---|
| **Problem** | Most music platforms focus on personal playlists/algorithms, so there is no shared “live” listening moment curated by a human. |
| **Solution** | A scheduled radio timeline with server-time based playback sync + API-first admin programming + simple UI for listeners. |
| **Target Users** | Listeners in small communities (students, local groups), and admins who curate music/schedules via API. |
| **Key Features** | 1) Global timeline playback (join at correct second)<br/> 2) “Now playing” with artwork <br/>3) Online listeners counter<br/> 4) Chat + user profile<br/> 5) API-based scheduling & playlist/track management |
| **Tech Stack** | Java 17, Spring Boot, PostgreSQL, Thymeleaf, Docker Compose, GitHub Actions, OpenAPI/Swagger, Trivy security scans |
