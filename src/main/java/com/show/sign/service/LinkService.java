package com.show.sign.service;


import com.show.sign.entity.Link;

public interface LinkService {
    String restoreUrl(String url);
    String save(Link link);

}
