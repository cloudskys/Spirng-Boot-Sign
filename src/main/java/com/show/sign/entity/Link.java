package com.show.sign.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Link implements Serializable {
   private Integer id;
   private String longUrl;
   private String shortUrl;
}
