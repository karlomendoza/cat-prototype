package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entities.News;

public interface NewsRepository extends JpaRepository<News, Integer> {
	
}
