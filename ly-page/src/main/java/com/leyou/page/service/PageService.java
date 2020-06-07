package com.leyou.page.service;

import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import com.leyou.pojo.*;
import com.leyou.vo.SpuBo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PageService {
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private TemplateEngine templateEngine;

    public Map<String, Object> loadModel(Long spuId) {
        HashMap<String, Object> model = new HashMap<>();
        SpuBo spu = goodsClient.querySpuById(spuId);
        List<Sku> skus = spu.getSkus();
        SpuDetail detail = goodsClient.querySpuDetailById(spuId);
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        List<SpecGroup> specs = specificationClient.queryGroupByCid(spu.getCid3());
        model.put("spu", spu);
        model.put("skus", skus);
        model.put("detail", detail);
        model.put("brand", brand);
        model.put("categories", categories);
        model.put("specs", specs);
        return model;
    }

    public void createHtml(Long spuId) {
        Context context = new Context();
        context.setVariables(loadModel(spuId));
        File dest = new File("/usr/share/nginx/html", spuId + ".html");
        if (dest.exists()) {
            dest.delete();
        }
        try (PrintWriter wrriter = new PrintWriter(dest, "UTF-8");) {
            templateEngine.process("item", context, wrriter);
        } catch (Exception e) {
            log.error("[静态页服务]生成静态页异常！", e);
        }

    }

    public void deleteHtml(Long spuId) {
        File dest = new File("/usr/share/nginx/html", spuId + ".html");
        if (dest.exists()) {
            dest.delete();
        }
    }
}
