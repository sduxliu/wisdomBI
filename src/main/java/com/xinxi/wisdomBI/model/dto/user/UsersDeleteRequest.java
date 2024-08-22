package com.xinxi.wisdomBI.model.dto.user;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 蒲月理想
 */
@Data
public class UsersDeleteRequest implements Serializable {

    //用户id
   List<Long> ids;

   private static final long serialVersionUID = 1L;
}
