package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.dto.CartDto;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.pojo.*;
import com.leyou.vo.SpuBo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;


    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        //分页
        PageHelper.startPage(page, rows);
        //过滤
        Example example = new Example(SpuBo.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }

        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        //默认排序
        example.setOrderByClause("last_update_time DESC");
        List<SpuBo> spus = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(spus)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        //解析分类与品牌名称
        loadCategoryAndBrandName(spus);

        //解析分页结果
        PageInfo<SpuBo> pageInfo = new PageInfo<>(spus);
        return new PageResult(pageInfo.getTotal(), spus);
    }

    private void loadCategoryAndBrandName(List<SpuBo> spus) {
        for (SpuBo spu : spus) {
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names, "/"));

            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }
    }

    @Transactional
    public void saveGoods(SpuBo spuBo) {
        //新增SPU
        spuBo.setId(null);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        spuBo.setSaleable(true);
        spuBo.setValid(false);

        int insert = spuMapper.insert(spuBo);
        if (insert != 1) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //新增SPUDETAIL
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());

        spuDetailMapper.insert(spuDetail);
        // 保存sku和库存信息
        saveSkuAndStock(spuBo);

        //发送mq消息
        amqpTemplate.convertAndSend("item.insert", spuBo.getId());
    }

    private void saveSkuAndStock(SpuBo spuBo) {
        //新增SKU
        List<Sku> skus = spuBo.getSkus();

        List<Stock> stockList = new ArrayList<Stock>();

        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spuBo.getId());
            //批量新增不能返回id，需要查询。
            int count = skuMapper.insert(sku);
            if (count != 1) {
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            //新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());

/*            count = stockMapper.insert(stock);
            if (count != 1) {
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }*/
            stockList.add(stock);
        }
        //批量新增库存
        int count = stockMapper.insertList(stockList);
        if (count != stockList.size()) {
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }

    }

    public SpuDetail querySpuDetailById(Long id) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(id);
        if (spuDetail == null) {
            throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
        }
        return spuDetail;
    }

    public List<Sku> querySkuListById(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skuList = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        //查询库存
//        for (Sku s : skuList) {
//            Stock stock = stockMapper.selectByPrimaryKey(s.getId());
//            if(stock==null){
//                throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
//            }
//            s.setStock(stock.getStock());
//        }
        List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
        loadStockInSku(skuList, ids);
        return skuList;
    }

    /**
     * 查询库存并封装
     * @param skuList sku集合
     * @param ids skuId集合
     */
    private void loadStockInSku(List<Sku> skuList, List<Long> ids) {
        List<Stock> stocks = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stocks)) {
            throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
        }
        //把stock转换成map key:skuId,value:stock
        Map<Long, Integer> stockMap = stocks.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));

        skuList.forEach(s -> s.setStock(stockMap.get(s.getId())));
    }

    @Transactional
    public void updateGoods(SpuBo spuBo) {
        if (spuBo.getId() == null) {
            throw new LyException(ExceptionEnum.GOODS_ID_CANNOT_BE_NULL);
        }
        Sku sku = new Sku();
        sku.setSpuId(spuBo.getId());
        //查询sku
        List<Sku> skuList = skuMapper.select(sku);
        if (!CollectionUtils.isEmpty(skuList)) {
            //删除sku
            skuMapper.delete(sku);
            //删除stock
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }
        //修改spu
        spuBo.setValid(null);
        spuBo.setSaleable(null);
        spuBo.setCreateTime(null);
        spuBo.setLastUpdateTime(new Date());
        int count = spuMapper.updateByPrimaryKeySelective(spuBo);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        //修改detail
        count = spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        //新增sku和stock
        saveSkuAndStock(spuBo);

        //发送mq消息
        amqpTemplate.convertAndSend("item.update", spuBo.getId());
    }

    public Spu querySpuById(Long id) {
        //查询spu
        SpuBo spuBo = spuMapper.selectByPrimaryKey(id);
        if (spuBo == null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //查询sku
        spuBo.setSkus(querySkuListById(id));
        //查询detail
        spuBo.setSpuDetail(querySpuDetailById(id));
        return spuBo;
    }

    public List<Sku> querySkuListByIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        loadStockInSku(skus,ids);
        return skus;
    }

    @Transactional
    public void decreaseStock(List<CartDto> carts) {
        for (CartDto cart : carts) {
            int count = stockMapper.decreaseStock(cart.getSkuId(), cart.getNum());
            if(count!=1){
                throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }
    }
}
