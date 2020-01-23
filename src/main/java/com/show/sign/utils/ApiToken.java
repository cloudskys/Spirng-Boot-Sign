package com.show.sign.utils;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiToken implements Serializable {
   private String urlstr;
   private String passmd5;
   private long expireTime;
}
