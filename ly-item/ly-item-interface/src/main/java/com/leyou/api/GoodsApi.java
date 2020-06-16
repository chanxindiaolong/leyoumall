package com.leyou.api;

import com.leyou.common.vo.PageResult;
import com.leyou.dto.CartDto;
import com.leyou.pojo.Sku;
import com.leyou.pojo.SpuDetail;
import com.leyou.vo.SpuBo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

public interface GoodsApi {
    @GetMapping("/spu/page")
    PageResult<SpuBo> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "6") Integer rows,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key);

    @GetMapping("/spu/detail/{id}")
    SpuDetail querySpuDetailById(@PathVariable("id") Long id);

    @GetMapping("/sku/list")
    List<Sku> querySkuListById(@RequestParam("id") Long id);

    @GetMapping("spu/{id}")
    SpuBo querySpuById(@PathVariable("id") Long id);

    @GetMapping("sku/list/ids")
    public List<Sku> querySkuListBySkuIds(@RequestParam("ids") List<Long> ids);

    @PostMapping("/stock/decrease")
    public void decreaseStock(@RequestBody List<CartDto> carts);
}
