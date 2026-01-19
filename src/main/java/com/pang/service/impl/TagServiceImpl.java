package com.pang.service.impl;

import com.pang.entity.vo.TagGroupVo;
import com.pang.entity.vo.TagVo;
import com.pang.mapper.TagMapper;
import com.pang.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;

    @Override
    public List<TagVo> listEnabled() {
        return tagMapper.listEnabled();
    }

    @Override
    public List<TagGroupVo> groupEnabled() {
        List<TagVo> list = tagMapper.listEnabled();

        Map<Integer, List<TagVo>> map = new LinkedHashMap<>();
        for (TagVo t : list) {
            map.computeIfAbsent(t.getType(), k -> new ArrayList<>()).add(t);
        }

        List<TagGroupVo> res = new ArrayList<>();
        for (var e : map.entrySet()) {
            TagGroupVo g = new TagGroupVo();
            g.setType(e.getKey());
            g.setTypeName(typeName(e.getKey()));
            g.setTags(e.getValue());
            res.add(g);
        }
        return res;
    }

    private String typeName(Integer type) {
        if (type == null) return "其他";
        return switch (type) {
            case 1 -> "主题";
            case 2 -> "场景";
            case 3 -> "心情";
            case 4 -> "语种";
            case 5 -> "风格";
            default -> "其他";
        };
    }
}
