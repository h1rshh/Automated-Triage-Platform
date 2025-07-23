🎯 Problem Statement
In a fast-paced production environment, developers and SREs spend a significant amount of time on the initial investigation of database performance alerts. This manual "triage" process—finding the alert, looking up the database schema, and locating the relevant source code—is repetitive, time-consuming, and delays the actual fix.

This project, the Automated Triage Platform, was built to solve this problem by automating the entire initial investigation, reducing a multi-minute manual task into a single-click action.

✨ Features
On-Demand Alert Fetching: A clean web UI to fetch recent alerts from any specified Slack channel.

Flexible Alert Parsing: A robust backend service that can parse multiple, complex alert formats using a flexible, pattern-based approach.

Automated Context Gathering: The system automatically enriches alerts with critical context:

Database Schema: Fetches the full schema for all tables implicated in a SQL query alert.

Code Context: Identifies the function mentioned in an alert and retrieves its source code and filename.

AI-Powered Analysis: Leverages the Google Gemini LLM to provide an expert-level root cause analysis, actionable recommendations, and an impact assessment based on all the gathered context.

Interactive Analysis Dashboard: A data-oriented UI that presents the final AI analysis alongside all the raw evidence (metadata, DB schema, code), allowing for deeper investigation.

Conversational Follow-up: A built-in chat interface that allows users to ask follow-up questions to the AI, maintaining the context of the original alert for a dynamic, co-pilot-like experience.

🛠️ Tech Stack
Backend: Java 17, Spring Boot 3

Database: MySQL 8.0

AI / LLM: Google Gemini API

External Integrations: Slack API

Frontend: HTML, Tailwind CSS, Vanilla JavaScript

Build Tool: Maven

🏗️ Architecture Overview
The platform is built using a modern microservice-inspired architecture, following the Model-Context-Protocol (MCP) philosophy. A central Spring Boot server acts as the MCP Host, orchestrating the gathering of a Model (the alert) and Context (from the database and codebase) to produce a final Analysis.
🚀 Getting Started
Follow these instructions to get the application running on your local machine.

Prerequisites
Java JDK 17 or later

Apache Maven

A running MySQL 8.0 instance

A Slack Bot Token with channels:history permissions

A Google Gemini API Key
