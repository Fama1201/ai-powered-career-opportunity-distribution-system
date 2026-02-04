package com.jobifycvut.backend.service;

import bot.api.OpportunityClient;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DefaultJobSearchClient implements JobSearchClient {
    @Override
    public Set<OpportunityClient.Opportunity> searchMultipleKeywords(String keywords) {
        return OpportunityClient.searchMultipleKeywords(keywords);
    }
}
