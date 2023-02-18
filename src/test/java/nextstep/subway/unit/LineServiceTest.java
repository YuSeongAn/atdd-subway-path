package nextstep.subway.unit;

import nextstep.subway.applicaion.LineService;
import nextstep.subway.applicaion.dto.SectionRequest;
import nextstep.subway.domain.Line;
import nextstep.subway.domain.LineRepository;
import nextstep.subway.domain.Section;
import nextstep.subway.domain.Sections;
import nextstep.subway.domain.Station;
import nextstep.subway.domain.StationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class LineServiceTest {
    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private LineRepository lineRepository;

    @Autowired
    private LineService lineService;

    @Test
    void addSection() {
        // given
        // stationRepository와 lineRepository를 활용하여 초기값 셋팅
        Line line = lineRepository.save(new Line("신분당선", "red"));
        Station upStation = stationRepository.save(new Station("양재역"));
        Station downStation = stationRepository.save(new Station("판교역"));

        // when
        // lineService.addSection 호출
        SectionRequest request = new SectionRequest(upStation.getId(), downStation.getId(), 10);
        lineService.addSection(line.getId(), request);

        // then
        // line.getSections 메서드를 통해 검증
        Sections sections = line.getSections();
        assertThat(sections.getSectionsCount()).isEqualTo(1);

        Section section = sections.getFirstSection(line);
        assertThat(section.getLine()).isEqualTo(line);
        assertThat(section.getUpStation()).isEqualTo(upStation);
        assertThat(section.getDownStation()).isEqualTo(downStation);
    }

}
