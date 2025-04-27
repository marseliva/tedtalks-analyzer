package com.example.tedtalksanalyzer.service;

import com.example.tedtalksanalyzer.dto.SearchRequest;
import com.example.tedtalksanalyzer.dto.TedTalkDTO;
import com.example.tedtalksanalyzer.model.TedTalk;
import com.example.tedtalksanalyzer.repository.TedTalkRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TedTalkService {

    private static final int BATCH_SIZE = 1000;

    private final TedTalkRepository tedTalkRepository;

    @Transactional
    public TedTalkDTO createTedTalk(TedTalk tedTalk) {
        return TedTalkMapper.toResponseDTO(tedTalkRepository.save(tedTalk));
    }

    @Transactional
    public void saveAll(final List<TedTalk> newRecords) {
        tedTalkRepository.saveAll(newRecords);
    }

    @Transactional(readOnly = true)
    public List<TedTalkDTO> getAllTedTalks() {
        return tedTalkRepository.findAll().stream()
                .map(TedTalkMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TedTalkDTO getTedTalkById(UUID id) {

        return tedTalkRepository.findById(id)
                .map(TedTalkMapper::toResponseDTO)
                .orElseThrow(() -> new IllegalArgumentException("TedTalk not found with id: " + id));
    }

    @Transactional
    public TedTalkDTO updateTedTalk(UUID id, TedTalk updatedTalk) {
        TedTalk tedTalk = tedTalkRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(updatedTalk.getTitle());
                    existing.setAuthor(updatedTalk.getAuthor());
                    existing.setDate(updatedTalk.getDate());
                    existing.setViews(updatedTalk.getViews());
                    existing.setLikes(updatedTalk.getLikes());
                    existing.setLink(updatedTalk.getLink());
                    return tedTalkRepository.save(existing);
                })
                .orElseThrow(() -> new IllegalArgumentException("TedTalk not found with id: " + id));
        return TedTalkMapper.toResponseDTO(tedTalk);
    }

    @Transactional
    public void deleteTedTalk(UUID id) {
        tedTalkRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<TedTalkDTO> search(SearchRequest request) {
        Specification<TedTalk> specification = buildSpecification(request);
        List<TedTalk> tedTalks = tedTalkRepository.findAll(specification);
        return tedTalks.stream()
                .map(TedTalkMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    private Specification<TedTalk> buildSpecification(SearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getAuthor() != null) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("author")), request.getAuthor().toLowerCase()));
            }
            if (request.getTitle() != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + request.getTitle().toLowerCase() + "%"));
            }
            if (request.getMinViews() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("views"), request.getMinViews()));
            }
            if (request.getMinLikes() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("likes"), request.getMinLikes()));
            }
            if (request.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), request.getStartDate()));
            }
            if (request.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), request.getEndDate()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public List<TedTalk> getTedTalksPage(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
        Slice<TedTalk> slice = tedTalkRepository.findAllBy(pageable);
        return slice.getContent();
    }
}
