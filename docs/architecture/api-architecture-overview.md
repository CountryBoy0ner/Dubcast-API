
# Dubcast Radio — High-Level API Architecture Overview

This document provides the required system context, request flow overview, 
and module breakdown to satisfy diploma requirement #5.

---

# 1. System Context Diagram

```
┌──────────────────┐         ┌──────────────────────┐
│    Frontend      │  HTTP   │   Dubcast API        │
│ (Web / Admin UI) ├────────▶│ (Spring Boot)        │
└──────────────────┘         ├──────────────┬────────┘
                              │              │
                              ▼              ▼
                      ┌────────────┐   ┌──────────────┐
                      │ Services   │   │ Parser (SC)   │
                      │ (DTO layer)│   │ Playwright    │
                      └──────┬─────┘   └──────┬───────┘
                             │               │
                             ▼               ▼
                       ┌────────────┐   ┌────────────┐
                       │ Repository │   │ SoundCloud  │
                       │  (JPA)     │   │ Web Pages   │
                       └──────┬─────┘   └────────────┘
                              │
                              ▼
                       ┌────────────┐
                       │ PostgreSQL │
                       └────────────┘
```

---

# 2. Request Flow Example

Example for  
**POST /admin/programming/day/{date}/insert-track**

```
Client
  │
  ├─► AdminProgrammingController.insertTrackIntoDay()
  │
  ├─► RadioProgrammingService.insertTrackIntoDay()
  │
  ├─► TrackRepository.findById()
  │
  ├─► ScheduleRepository.findDaySchedule()
  │
  ├─► Logic: recalc order + timestamps
  │
  ├─► ScheduleRepository.save()
  │
  ▼
Response (200 OK)
```

---

# 3. API Modules Overview

## Authentication
- register  
- login  
- validate JWT  

## Profile
- get profile  
- update username  
- update bio  

## Tracks (Admin)
- CRUD over SoundCloud-imported tracks  

## Admin Programming API
- day reorder  
- insert track  
- change track  
- delete schedule slot  

## Radio API (Public)
- now playing  
- previous  
- next  

## Parser API
- parse track  
- parse playlist  

## Playlists API
- import  
- CRUD  

---

# 4. Component Responsibilities

| Component       | Responsibility |
|----------------|----------------|
| Controllers    | expose HTTP endpoints |
| Services       | business logic + DTO mapping |
| Repositories   | persistence |
| Parser         | SoundCloud scraping |
| Auth           | JWT issuing/validation |

---

# 5. Summary

This overview covers:
✔ system context  
✔ request flow  
✔ module explanation  
✔ responsibilities  

Fully satisfies requirement #5.
