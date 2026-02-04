package com.jobifycvut.backend.service;

import bot.api.OpportunityClient;

import java.util.Set;

public interface JobSearchClient {
    Set<OpportunityClient.Opportunity> searchMultipleKeywords(String keywords);
}
