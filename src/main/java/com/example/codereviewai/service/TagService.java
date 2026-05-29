package com.example.codereviewai.service;

import com.example.codereviewai.dto.request.TagRequest;
import com.example.codereviewai.dto.response.TagResponse;
import com.example.codereviewai.entity.CodeSnippet;
import com.example.codereviewai.entity.Tag;
import com.example.codereviewai.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final SnippetService snippetService;

    private TagResponse toResponse(Tag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        return response;
    }

    public TagResponse addTag(Long snippetId, TagRequest request) {
        CodeSnippet snippet = snippetService.getSnippetEntityById(snippetId);
        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setCodeSnippet(snippet);
        return toResponse(tagRepository.save(tag));
    }

    public List<TagResponse> getTagsBySnippet(Long snippetId) {
        return tagRepository.findByCodeSnippetId(snippetId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }
}