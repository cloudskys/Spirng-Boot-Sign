package com.show.sign.service.impl;

import com.alibaba.fastjson.JSON;
import com.show.sign.dao.LinkMapper;
import com.show.sign.service.LinkService;
import com.show.sign.entity.Link;
import com.show.sign.utils.ShortUrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LinkServiceImpl implements LinkService {
    @Autowired
    private  LinkMapper linkMapper;


    @Override
    public String save(Link link) {
        String shortUrl = "http://127.0.0.1:8080/signTest/";
        String longUrl = link.getLongUrl();
        System.out.println(longUrl);
        Link link1 = linkMapper.findByLongUrl(longUrl);
        if(link1 == null) {
            shortUrl += ShortUrlUtil.gererateShortUrl(longUrl);
            link.setShortUrl(shortUrl);
            linkMapper.insert(link);
        }else{
            shortUrl = link1.getShortUrl();
        }
        log.info(JSON.toJSONString(link));
        return shortUrl;
    }
    @Override
    public String restoreUrl(String url) {
        return linkMapper.findByShortUrl(url);

    }
}
