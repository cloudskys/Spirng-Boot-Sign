package com.show.sign.dao;


import com.show.sign.entity.Link;

public interface LinkMapper {
    Link selectByPrimaryKey(Integer id);

    int insert(Link link);

    Link findByLongUrl(String longUrl);

    String findByShortUrl(String shortUrl);
}
