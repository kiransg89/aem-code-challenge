package com.anf.core.beans;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

@Data
public class NewsData {
	/***** Begin Code - Kiran SG *****/
	private String title;
	private String description;
	private String content;
	private String author;
	private String urlImage;
	private String url;
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	LocalDateTime now = LocalDateTime.now();
	private String date = dtf.format(now);
	/**** END Code *****/
}