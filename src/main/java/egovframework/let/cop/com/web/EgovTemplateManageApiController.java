package egovframework.let.cop.com.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.SessionStatus;
import org.springmodules.validation.commons.DefaultBeanValidator;

import egovframework.com.cmm.ComDefaultCodeVO;
import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.ResponseCode;
import egovframework.com.cmm.service.EgovCmmUseService;
import egovframework.com.cmm.service.ResultVO;
import egovframework.com.cmm.util.EgovUserDetailsHelper;
import egovframework.let.cop.com.service.EgovTemplateManageService;
import egovframework.let.cop.com.service.TemplateInf;
import egovframework.let.cop.com.service.TemplateInfVO;

/**
 * 템플릿 관리를 위한 컨트롤러 클래스
 * @author 공통서비스개발팀 이삼섭
 * @since 2009.03.18
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.03.18  이삼섭          최초 생성
 *   2011.08.31  JJY            경량환경 템플릿 커스터마이징버전 생성
 *
 * </pre>
 */
@RestController
public class EgovTemplateManageApiController {

	@Resource(name = "EgovTemplateManageService")
	private EgovTemplateManageService tmplatService;

	@Resource(name = "EgovCmmUseService")
	private EgovCmmUseService cmmUseService;

	@Resource(name = "propertiesService")
	protected EgovPropertyService propertyService;

	@Autowired
	private DefaultBeanValidator beanValidator;

	/** EgovMessageSource */
	@Resource(name = "egovMessageSource")
	EgovMessageSource egovMessageSource;

	/**
	 * 템플릿 목록을 조회한다.
	 *
     * @param tmplatInfVO
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value ="/cop/com/selectTemplateInfsAPI.do")
	public ResultVO selectTemplateInfs(@RequestBody TemplateInfVO tmplatInfVO)
		throws Exception {
		ResultVO resultVO = new ResultVO();

		if (!EgovUserDetailsHelper.isAuthenticated()) {
			return handleAuthError(resultVO); // server-side 권한 확인
		}

		LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();

		tmplatInfVO.setPageUnit(propertyService.getInt("pageUnit"));
		tmplatInfVO.setPageSize(propertyService.getInt("pageSize"));

		PaginationInfo paginationInfo = new PaginationInfo();

		paginationInfo.setCurrentPageNo(tmplatInfVO.getPageIndex());
		paginationInfo.setRecordCountPerPage(tmplatInfVO.getPageUnit());
		paginationInfo.setPageSize(tmplatInfVO.getPageSize());

		tmplatInfVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
		tmplatInfVO.setLastIndex(paginationInfo.getLastRecordIndex());
		tmplatInfVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

		Map<String, Object> resultMap = tmplatService.selectTemplateInfs(tmplatInfVO);
		int totCnt = Integer.parseInt((String)resultMap.get("resultCnt"));

		paginationInfo.setTotalRecordCount(totCnt);

		resultMap.put("paginationInfo", paginationInfo);
		resultMap.put("user", user);

		resultVO.setResult(resultMap);
		resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
		resultVO.setResultMessage(ResponseCode.SUCCESS.getMessage());

		return resultVO;
	}

	/**
	 * 템플릿에 대한 상세정보를 조회한다.
	 *
     * @param tmplatInfVO
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value ="/cop/com/selectTemplateInfAPI.do")
	public ResultVO selectTemplateInf(@RequestBody TemplateInfVO tmplatInfVO)
		throws Exception {
		ResultVO resultVO = new ResultVO();

		if (!EgovUserDetailsHelper.isAuthenticated()) {
			return handleAuthError(resultVO); // server-side 권한 확인
		}

		ComDefaultCodeVO codeVO = new ComDefaultCodeVO();

		codeVO.setCodeId("COM005");
		List<?> result = cmmUseService.selectCmmCodeDetail(codeVO);

		TemplateInfVO vo = tmplatService.selectTemplateInf(tmplatInfVO);

		Map<String, Object> resultMap = new HashMap<String, Object>();

		resultMap.put("templateInfVO", vo);
		resultMap.put("resultList", result);

		resultVO.setResult(resultMap);
		resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
		resultVO.setResultMessage(ResponseCode.SUCCESS.getMessage());

		return resultVO;
	}

	/**
	 * 템플릿 정보를 등록한다.
	 *
	 * @param searchVO
     * @param templateInf
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value ="/cop/com/insertTemplateInfAPI.do")
	public ResultVO insertTemplateInf(@ModelAttribute("searchVO") TemplateInfVO searchVO,
		@ModelAttribute("templateInf") TemplateInf templateInf,
		BindingResult bindingResult, SessionStatus status) throws Exception {
		ResultVO resultVO = new ResultVO();
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if (!EgovUserDetailsHelper.isAuthenticated()) {
			return handleAuthError(resultVO); // server-side 권한 확인
		}

		LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
		Boolean isAuthenticated = !EgovUserDetailsHelper.isAuthenticated();

		beanValidator.validate(templateInf, bindingResult);

		if (bindingResult.hasErrors()) {
			ComDefaultCodeVO vo = new ComDefaultCodeVO();

			vo.setCodeId("COM005");

			List<?> codeResult = cmmUseService.selectCmmCodeDetail(vo);
			
			resultMap.put("typeList", codeResult);
			
			vo.setCodeId("COM009");

			codeResult = cmmUseService.selectCmmCodeDetail(vo);

			resultMap.put("attrbList", codeResult);

			resultVO.setResult(resultMap);
			resultVO.setResultCode(ResponseCode.INPUT_CHECK_ERROR.getCode());
			resultVO.setResultMessage(ResponseCode.INPUT_CHECK_ERROR.getMessage());
			
			return resultVO;
		}

		templateInf.setFrstRegisterId(user.getUniqId());

		if (isAuthenticated) {
			tmplatService.insertTemplateInf(templateInf);
			
			resultVO.setResult(resultMap);
			resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
			resultVO.setResultMessage(ResponseCode.SUCCESS.getMessage());
		}

		return resultVO;
	}

	/**
	 * 템플릿 정보를 수정한다.
	 *
     * @param tmplatInfVO
     * @param templateInf
     * @param bindingResult
     * @return ResultVO
	 * @throws Exception
	 */
	@PostMapping(value ="/cop/com/updateTemplateInfAPI.do")
	public ResultVO updateTemplateInf(@ModelAttribute("searchVO") TemplateInfVO tmplatInfVO,
		@ModelAttribute("templateInf") TemplateInf templateInf,
		BindingResult bindingResult, SessionStatus status) throws Exception {
		ResultVO resultVO = new ResultVO();
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if (!EgovUserDetailsHelper.isAuthenticated()) {
			return handleAuthError(resultVO); // server-side 권한 확인
		}

		LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
		Boolean isAuthenticated = !EgovUserDetailsHelper.isAuthenticated();

		beanValidator.validate(templateInf, bindingResult);

		if (bindingResult.hasErrors()) {
			ComDefaultCodeVO codeVO = new ComDefaultCodeVO();

			codeVO.setCodeId("COM005");

			List<?> result = cmmUseService.selectCmmCodeDetail(codeVO);

			TemplateInfVO vo = tmplatService.selectTemplateInf(tmplatInfVO);

			resultMap.put("TemplateInfVO", vo);
			resultMap.put("resultList", result);
			
			resultVO.setResult(resultMap);
			resultVO.setResultCode(ResponseCode.INPUT_CHECK_ERROR.getCode());
			resultVO.setResultMessage(ResponseCode.INPUT_CHECK_ERROR.getMessage());

			return resultVO;
		}

		templateInf.setLastUpdusrId(user.getUniqId());

		if (isAuthenticated) {
			tmplatService.updateTemplateInf(templateInf);
			
			resultVO.setResult(resultMap);
			resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
			resultVO.setResultMessage(ResponseCode.SUCCESS.getMessage());
		}

		return resultVO;
	}

	/**
	 * 템플릿 정보를 삭제한다.
	 *
	 * @param searchVO
     * @param tmplatInf
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value ="/cop/bbs/deleteTemplateInfAPI.do")
	public ResultVO deleteTemplateInf(@ModelAttribute("searchVO") TemplateInfVO searchVO,
		@ModelAttribute("tmplatInf") TemplateInf tmplatInf,
		SessionStatus status) throws Exception {
		ResultVO resultVO = new ResultVO();

		if (!EgovUserDetailsHelper.isAuthenticated()) {
			return handleAuthError(resultVO); // server-side 권한 확인
		}

		LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
		Boolean isAuthenticated = !EgovUserDetailsHelper.isAuthenticated();

		tmplatInf.setLastUpdusrId(user.getUniqId());

		if (isAuthenticated) {
			tmplatService.deleteTemplateInf(tmplatInf);
			
			resultVO.setResultCode(ResponseCode.SUCCESS.getCode());
			resultVO.setResultMessage(ResponseCode.SUCCESS.getMessage());
		}

		return resultVO;
	}
	
	private ResultVO handleAuthError(ResultVO resultVO) {
		resultVO.setResultCode(ResponseCode.AUTH_ERROR.getCode());
		resultVO.setResultMessage(ResponseCode.AUTH_ERROR.getMessage());
		return resultVO;
	}

	/**
	 * 운영자 권한을 확인한다.(로그인 여부를 확인한다.)
	 *
	 * @throws EgovBizException
	 */
	protected boolean checkAuthority() throws Exception {
		// 사용자권한 처리
		if (!EgovUserDetailsHelper.isAuthenticated()) {
			return false;
		} else {
			return true;
		}
	}
}
