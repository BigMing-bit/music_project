package com.pang.service;

import com.pang.entity.Singer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SingerServiceTest {

    @Autowired
    private SingerService singerService;


    @Test
    void testSaveSinger() {
        Singer singer = new Singer();
        singer.setName("张三");
        singer.setGender(1);
        singer.setArea(1);
        singer.setStyle(1);
        singer.setAvatarUrl("https://picsum.photos/200/300");
        singer.setIntro("张三");
        singerService.save(singer);
    }

    @Test
    void testQuerySinger() {
        List<Singer> singers = singerService.listByIds(List.of(1L, 2L, 5L));
        singers.forEach(System.out::println);
    }

}