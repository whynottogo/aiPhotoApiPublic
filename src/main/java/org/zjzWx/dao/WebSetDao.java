package org.zjzWx.dao;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.zjzWx.entity.WebSet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WebSetDao extends BaseMapper<WebSet> {


    @Select("SELECT download_one,download_two,video_unit_id,home_left_info,home_left_url FROM web_set LIMIT 1")
    WebSet getWeb();


}
