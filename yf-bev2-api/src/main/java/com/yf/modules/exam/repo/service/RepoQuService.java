package com.yf.modules.exam.repo.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yf.base.api.api.dto.PagingReqDTO;
import com.yf.modules.exam.repo.dto.ImportResult;
import com.yf.modules.exam.repo.dto.RepoQuDTO;
import com.yf.modules.exam.repo.dto.request.RepoQuDetailDTO;
import com.yf.modules.exam.repo.dto.request.RepoQuListReqDTO;
import com.yf.modules.exam.repo.dto.request.ReviewReqDTO;
import com.yf.modules.exam.repo.entity.RepoQu;

import java.util.List;

/**
 * <p>
 * 问题题目业务接口类
 * </p>
 *
 * @author 聪明笨狗
 * @since 2025-04-11 09:42
 */
public interface RepoQuService extends IService<RepoQu> {

    /**
     * 分页查询数据
     *
     * @param reqDTO
     * @return
     */
    IPage<RepoQuDTO> paging(PagingReqDTO<RepoQuListReqDTO> reqDTO);

    /**
     * 添加或保存
     *
     * @param reqDTO
     * @return
     */
    void save(RepoQuDetailDTO reqDTO);

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    void delete(List<String> ids);

    /**
     * 查找详情
     *
     * @param id
     * @return
     */
    RepoQuDetailDTO detail(String id);

    /**
     * 查找列表
     *
     * @param reqDTO
     * @return
     */
    List<RepoQuDTO> list(RepoQuDTO reqDTO);

    /**
     * 查找题目列表用于组卷
     *
     * @param repoId
     * @param quType
     * @param quCount
     * @return
     */
    List<RepoQuDetailDTO> listForPaper(String repoId, String quType, Integer quCount);

    /**
     * 两级审核：DRAFT→一审→二审→APPROVED/REJECTED
     * 约束：不可审自己创建的题目；一二审不可为同一人
     *
     * @param reqDTO 审核请求
     */
    void review(ReviewReqDTO reqDTO);

    /**
     * 一键审核（总护理长/管理员）：批量置 APPROVED
     *
     * @param ids 题目ID列表
     */
    void batchApprove(List<String> ids);

    /**
     * 随机抽题用于练习（仅取已通过审核 APPROVED 的题目）
     *
     * @param repoId  题库ID，可空
     * @param quType  题型，可空
     * @param count   抽取数量，可空
     * @return 题目详情列表
     */
    List<RepoQuDetailDTO> randomForPractice(String repoId, String quType, Integer count);

    /**
     * Excel 批量导入题目（含 XXE 防护）
     *
     * @param repoId 目标题库
     * @param file   上传的 Excel
     * @return 导入结果
     */
    ImportResult importExcel(String repoId, org.springframework.web.multipart.MultipartFile file);

    /**
     * 统一导入入口：自动识别 xls/xlsx/docx
     *
     * @param repoId 目标题库
     * @param file   上传文件
     * @return 导入结果
     */
    ImportResult importFile(String repoId, org.springframework.web.multipart.MultipartFile file);

    /**
     * Word(.docx) 批量导入题目（血透中心考核文档排版）
     *
     * @param repoId 目标题库
     * @param file   上传的 docx
     * @return 导入结果
     */
    ImportResult importWord(String repoId, org.springframework.web.multipart.MultipartFile file);
}

