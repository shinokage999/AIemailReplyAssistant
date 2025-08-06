package com.shinokage.aiemailassistant.Service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinokage.aiemailassistant.Model.EmailRequest;


@Service
public class EmailGeneratorService {
    
    private final WebClient webclient;

    private String geminiApiUrl="https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";
    private String geminiApikey= "AIzaSyA6F1YYXchZdq9M5-baWHtKSvhQ-_w5QCQ";

    public EmailGeneratorService(WebClient.Builder webclientBuilder) {
        this.webclient = webclientBuilder.build();
    }

    public String generateReply(EmailRequest emailRequest) {
       //prompt
        String prompt = buildPrompt(emailRequest);
        //request
        Map<String , Object> requestBody=Map.of(
            "contents",new Object[]{
           Map.of("parts",new Object[]{
            Map.of("text",prompt)
           })
            }
        );
        
        //response
        String response =  webclient.post()
        .uri(geminiApiUrl+geminiApikey)
        .header("Content-Type","application/json")
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class)
        .block();
        
        // extract and return response
        return extractResponseContent(response);
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional Email reply for the following email content. PLease do not add a subject line.");
        if(emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
            prompt.append(" The tone of the reply should be ").append(emailRequest.getTone());
    }
    prompt.append("\n Original Email \n").append(emailRequest.getEmailContent());
    return prompt.toString();
 }

    private String extractResponseContent(String response) {
     try{
          ObjectMapper mapper = new ObjectMapper();
          JsonNode rootNode = mapper.readTree(response);
          return rootNode.path("candidates")
          .get(0)
          .path("content")
          .path("parts")
          .get(0)
          .path("text")
          .asText();
     } catch (Exception e){
        return "Error processing request :" + e.getMessage();
     }
    }
}
