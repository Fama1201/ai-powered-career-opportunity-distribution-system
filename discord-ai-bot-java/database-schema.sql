-- Database Schema for Jobify CVUT Backend
-- Run this script on your Neon PostgreSQL database

-- Users table for student authentication
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP,
    last_login_at TIMESTAMP,
    email_verified_at TIMESTAMP,
    password_reset_token VARCHAR(255),
    password_reset_token_expiration TIMESTAMP,
    email_verification_token VARCHAR(255)
);

-- HR Users table
CREATE TABLE IF NOT EXISTS hr (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    company_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Student table (for Discord bot integration)
CREATE TABLE IF NOT EXISTS student (
    id SERIAL PRIMARY KEY,
    name TEXT,
    email TEXT,
    skills TEXT,
    career_interest TEXT,
    discord_id TEXT UNIQUE,
    cv_text TEXT
);

-- Opportunities table
CREATE TABLE IF NOT EXISTS opportunities (
    opportunity_id TEXT,
    discord_id TEXT,
    title TEXT,
    description TEXT,
    job_type TEXT,
    application_deadline DATE,
    url TEXT,
    wage TEXT,
    home_office TEXT,
    benefits TEXT,
    formal_requirements TEXT,
    technical_requirements TEXT,
    contact_person TEXT,
    company TEXT,
    PRIMARY KEY (opportunity_id, discord_id)
);

-- Feedback table
CREATE TABLE IF NOT EXISTS feedback (
    id SERIAL PRIMARY KEY,
    feedback_text TEXT,
    stars INTEGER,
    discord_id TEXT
);

-- Job Applications table
CREATE TABLE IF NOT EXISTS job_applications (
    id SERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    opportunity_id BIGINT,
    status VARCHAR(50) DEFAULT 'PENDING',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id SERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    message TEXT,
    type VARCHAR(50),
    read BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Saved Jobs table
CREATE TABLE IF NOT EXISTS saved_jobs (
    id SERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    opportunity_id BIGINT,
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Interactions table (for AI chat history)
CREATE TABLE IF NOT EXISTS interactions (
    id SERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(100),
    prompt TEXT,
    response TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

