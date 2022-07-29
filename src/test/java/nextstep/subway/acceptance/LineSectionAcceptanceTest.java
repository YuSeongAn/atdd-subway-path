package nextstep.subway.acceptance;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import static nextstep.subway.acceptance.LineSteps.지하철_노선_생성_요청;
import static nextstep.subway.acceptance.LineSteps.지하철_노선_조회_요청;
import static nextstep.subway.acceptance.LineSteps.지하철_노선에_지하철_구간_생성_요청;
import static nextstep.subway.acceptance.LineSteps.지하철_노선에_지하철_구간_제거_요청;
import static nextstep.subway.acceptance.StationSteps.지하철역_생성_요청;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 구간 관리 기능")
class LineSectionAcceptanceTest extends AcceptanceTest {
	private Long 신분당선;
	private Long 강남역;
	private Long 양재역;

	/**
	 * Given 지하철역과 노선 생성을 요청 하고
	 */
	@BeforeEach
	public void setUp() {
		super.setUp();

		강남역 = 지하철역_생성_요청("강남역").jsonPath().getLong("id");
		양재역 = 지하철역_생성_요청("양재역").jsonPath().getLong("id");

		Map<String, String> lineCreateParams = createLineCreateParams(강남역, 양재역);
		신분당선 = 지하철_노선_생성_요청(lineCreateParams).jsonPath().getLong("id");
	}

	/**
	 * When 지하철 노선에 새로운 구간 추가를 요청 하면
	 * Then 노선에 새로운 구간이 추가된다
	 */
	@DisplayName("지하철 노선에 구간을 등록")
	@Test
	void addLineSection() {
		// when
		Long 정자역 = 지하철역_생성_요청("정자역").jsonPath().getLong("id");
		지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재역, 정자역));

		// then
		ExtractableResponse<Response> response = 지하철_노선_조회_요청(신분당선);
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.jsonPath().getList("stations.id", Long.class)).containsExactly(강남역, 양재역, 정자역);
	}

	/**
	 * Given 지하철 노선에 새로운 구간 추가를 요청 하고
	 * When 지하철 노선의 마지막 구간 제거를 요청 하면
	 * Then 노선에 구간이 제거된다
	 */
	@DisplayName("지하철 노선에 구간을 제거")
	@Test
	void removeLineSection() {
		// given
		Long 정자역 = 지하철역_생성_요청("정자역").jsonPath().getLong("id");
		지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재역, 정자역));

		// when
		지하철_노선에_지하철_구간_제거_요청(신분당선, 정자역);

		// then
		ExtractableResponse<Response> response = 지하철_노선_조회_요청(신분당선);
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.jsonPath().getList("stations.id", Long.class)).contains(강남역, 양재역);
	}

	/**
	 * given 노선이 생성되어 있음 and 구간이 생성되어 있음 (강남역 - 양재역)
	 * when 판교역을 기준으로 (강남역 - 청계산입구역) 구간을 추가함
	 * then 구간 조회시 2개의 구간(강남역 - 양재역, 양재역 - 청계산입구역)이 조회됨
	 */
	@DisplayName("기존 구간의 역을 기준으로 새로운 구간을 추가")
	@Test
	void addNewSectionBasedExistingSection() {

		//given
		Long 청계산입구역 = 지하철역_생성_요청("청계산입구역").jsonPath().getLong("id");

		//when
		지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(강남역, 청계산입구역));

		//then
		ExtractableResponse<Response> response = 지하철_노선_조회_요청(신분당선);
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.jsonPath().getList("stations.id", Long.class)).contains(강남역, 양재역, 청계산입구역);
	}

	/**
	 * given 노선이 생성되어 있음 and 구간이 생성되어 있음 (강남역 - 양재역)
	 * when 판교역을 기준으로 (강남역 - 청계산입구역) 구간을 추가함 근데 강남역-청계산입구역 거리가 강남역 - 양재역 거리보다 김
	 * then 에러 발생
	 */
	@DisplayName("기존 구간의 역을 기준으로 구간을 추가하는데 새로운 구간이 기존 구간거리보다 길경우 에러 발생")
	@Test
	void addNewSectionBasedExistingSectionException() {

		//given
		Long 청계산입구역 = 지하철역_생성_요청("청계산입구역").jsonPath().getLong("id");

		//when
		ExtractableResponse<Response> response = 지하철_노선에_지하철_구간_생성_요청(신분당선, createIllegalSectionCreateParams(강남역, 청계산입구역, 19));

		//then
		assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		assertThat(response.body().asString()).contains("추가하려는 구간의 길이는 기존 구간의 길이와 같거나 길수 없습니다");
	}

	/**
	 * given 노선이 생성되어 있음 and 구간이 생성되어 있음 (강남역 - 양재역)
	 * when 신논현역 - 강남역 구간을 등록
	 * then 구간 조회시 신논현역 - 강남역 구간이 조회됨
	 */
	@DisplayName("새로운 역을 상행 종점으로 구간 등록")
	@Test
	void addNewUpStationSection() {

		//given
		Long 신논현역 = 지하철역_생성_요청("신논현역").jsonPath().getLong("id");

		//when
		지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(신논현역, 강남역));

		//then
		ExtractableResponse<Response> response = 지하철_노선_조회_요청(신분당선);
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.jsonPath().getList("stations.id", Long.class)).contains(강남역, 양재역, 신논현역);
	}

	/**
	 * given 노선이 생성되어 있음 and 구간이 생성되어 있음 (강남역 - 양재역)
	 * when 양재역-양재시민의숲역 구간을 등록
	 * then 구간 조회시 양재역-양재시민의숲역 구간이 조회됨
	 */
	@DisplayName("새로운 역을 하행 종점으로 구간 등록")
	@Test
	void addNewDownStationSection() {

		//given
		Long 양재시민의숲역 = 지하철역_생성_요청("양재시민의숲역").jsonPath().getLong("id");

		//when
		지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재역, 양재시민의숲역));

		//then
		ExtractableResponse<Response> response = 지하철_노선_조회_요청(신분당선);
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.jsonPath().getList("stations.id", Long.class)).contains(강남역, 양재역, 양재시민의숲역);
	}

	/**
	 * given 노선이 생성되어 있음 and 구간이 생성되어 있음 (강남역 - 양재역)
	 * when 판교역을 기준으로 (양재시민의숲역 - 양재역) 구간을 추가함
	 * then 구간 조회시 2개의 구간(강남역 - 양재시민의숲역, 양재시민의숲역 - 양재역)이 조회됨
	 */
	@DisplayName("기존 구간의 역을 기준으로 새로운 역추가(하행역 기준)")
	@Test
	void addNewSectionBasedExistingSectionDownStation() {
		//given
		Long 양재시민의숲역 = 지하철역_생성_요청("양재시민의숲역").jsonPath().getLong("id");

		//when
		지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재시민의숲역, 양재역));

		//then
		ExtractableResponse<Response> response = 지하철_노선_조회_요청(신분당선);
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.jsonPath().getList("stations.id", Long.class)).contains(강남역, 양재역, 양재시민의숲역);

	}

	@DisplayName("상행역과 하행역이 모두 노선에 추가되어있다면 구간 추가 불가")
	@Test
	void exceptionAlreadyExistStation() {

		//given //when
		ExtractableResponse<Response> response = 지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(강남역, 양재역));

		//then
		assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		assertThat(response.body().asString()).contains("상행역과 하행역이 모두 등록되어있습니다.");
	}

	@DisplayName("상행역과 하행역 둘중 하나라도 포함되어있지 않다면 구간 추가 불가")
	@Test
	void exceptionNoStation() {

		//given
		Long 양재시민의숲역 = 지하철역_생성_요청("양재시민의숲역").jsonPath().getLong("id");
		Long 판교역 = 지하철역_생성_요청("판교역").jsonPath().getLong("id");

		//when
		ExtractableResponse<Response> response = 지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재시민의숲역, 판교역));

		//then
		assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
		assertThat(response.body().asString()).contains("구간추가가 불가합니다.");
	}

	/**
	 * given 노선이 생성되어 있음 and 구간이 생성되어 있음(강남역 - 양재역 - 판교역)
	 * when 구간을 삭제함(양재역)
	 * then 노선의 구간 조회시 양재역이 조회되지 않음
	 */
	@DisplayName("구간을 삭제하는 테스트")
	@Test
	void deleteSectionTest() {

		//given
		Long 판교역 = 지하철역_생성_요청("판교역").jsonPath().getLong("id");
		지하철_노선에_지하철_구간_생성_요청(신분당선, createSectionCreateParams(양재역, 판교역));

		//when
		지하철_노선에_지하철_구간_제거_요청(신분당선, 양재역);

		//then
		ExtractableResponse<Response> response = 지하철_노선_조회_요청(신분당선);
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.jsonPath().getList("stations.id", Long.class)).contains(강남역, 판교역);
	}

	/**
	 * given 노선이 생성되어 있음 and 구간이 생성되어 있음 (강남역 - 양재역)
	 * when 구간을 삭제함(양재역)
	 * then 구간 삭제 실패
	 */
	@DisplayName("구간이 하나뿐인 노선에서 구간을 삭제할때 에러 발생 테스트")
	@Test
	void deleteOnlyOneSectionTest() {

		//given //when
		ExtractableResponse<Response> response = 지하철_노선에_지하철_구간_제거_요청(신분당선, 양재역);

		//then
		assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	private Map<String, String> createLineCreateParams(Long upStationId, Long downStationId) {
		Map<String, String> lineCreateParams;
		lineCreateParams = new HashMap<>();
		lineCreateParams.put("name", "신분당선");
		lineCreateParams.put("color", "bg-red-600");
		lineCreateParams.put("upStationId", upStationId + "");
		lineCreateParams.put("downStationId", downStationId + "");
		lineCreateParams.put("distance", 10 + "");
		return lineCreateParams;
	}

	private Map<String, String> createSectionCreateParams(Long upStationId, Long downStationId) {
		Map<String, String> params = new HashMap<>();
		params.put("upStationId", upStationId + "");
		params.put("downStationId", downStationId + "");
		params.put("distance", 6 + "");
		return params;
	}

	private Map<String, String> createIllegalSectionCreateParams(Long upStationId, Long downStationId, int distance) {
		Map<String, String> params = new HashMap<>();
		params.put("upStationId", upStationId + "");
		params.put("downStationId", downStationId + "");
		params.put("distance", distance + "");
		return params;
	}
}
