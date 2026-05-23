package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.result.PageResult;
import com.example.backend.dto.request.UserCreateRequest;
import com.example.backend.dto.request.UserUpdateRequest;
import com.example.backend.dto.response.UserVO;
import com.example.backend.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    PageResult<UserVO> page(Integer page, Integer size, String keyword, String role, Integer status);

    UserVO getDetail(Long id);

    void create(UserCreateRequest request);

    void update(Long id, UserUpdateRequest request);

    void delete(Long id);

    void resetPassword(Long id);
}
