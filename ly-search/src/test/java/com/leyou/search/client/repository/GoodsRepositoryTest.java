package com.leyou.search.client.repository;

import com.leyou.common.vo.PageResult;
import com.leyou.pojo.Spu;
import com.leyou.search.Goods;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.service.SearchService;
import com.leyou.vo.SpuBo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SearchService searchService;

    @Test
    public void testCreateIndex() {
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
    }

    @Test
    public void loadData() {
        int page = 1;
        int rows = 100;
        int size = 0;
        do {
            PageResult<SpuBo> result = goodsClient.querySpuByPage(page, rows, true, null);
            List<SpuBo> items = result.getItems();
            if (CollectionUtils.isEmpty(items)) {
                break;
            }
/*        for (SpuBo item : items) {
            searchService.buildGoods(item);
        }*/
            List<Goods> goodsList = items.stream().map(searchService::buildGoods).collect(Collectors.toList());
            goodsRepository.saveAll(goodsList);
            page++;
            size = items.size();
        } while (size == 100);
    }
}