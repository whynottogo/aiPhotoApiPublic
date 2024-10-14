package org.zjzWx.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Shynin
 * @version 1.0
 * @date 2024/10/11 15:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("pic_info")
public class PicInfo {

    private long id;

    private String pic;

    private String info1;

    private String info2;
    private String info3;
    private String title;
    private Integer deleted;
}
