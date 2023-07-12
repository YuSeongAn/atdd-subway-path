package subway.domain;

import lombok.*;
import subway.exception.StationLineCreateException;
import subway.exception.StationLineSectionCreateException;
import subway.exception.StationLineSectionDeleteException;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "lineId")
public class StationLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lineId;

    @Column
    private String name;

    @Column
    private String color;

    @Embedded
    private StationLineSections sections;

    @Builder
    public StationLine(String name, String color, Station upStation, Station downStation, BigDecimal distance) {
        if (upStation.equals(downStation)) {
            throw new StationLineCreateException("upStation and downStation can't be equal");
        }

        this.name = name;
        this.color = color;

        final StationLineSection section = StationLineSection.builder()
                .upStation(upStation)
                .downStation(downStation)
                .distance(distance)
                .build();

        sections = StationLineSections.builder()
                .section(section)
                .build();

        section.apply(this);
    }

    public void update(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public StationLineSection createSection(Station sectionUpStation, Station sectionDownStation, BigDecimal distance) {
        checkSectionStationExistOnlyOneToLine(sectionUpStation, sectionDownStation);

        final StationLineSection newSection = sections.appendStationLineSection(sectionUpStation, sectionDownStation, distance);

        newSection.apply(this);
        return newSection;
    }

    private void checkSectionStationExistOnlyOneToLine(Station sectionUpStation, Station sectionDownStation) {
        if (isStationExistingToLine(sectionUpStation) == isStationExistingToLine(sectionDownStation)) {
            throw new StationLineCreateException("one of section up station and down station exactly exist only one to line");
        }
    }

    private boolean isStationExistingToLine(Station station) {
        return getAllStations().stream()
                .anyMatch(station::equals);
    }

    public void deleteSection(Station station) {
        sections.deleteSection(station);
    }

    public List<Station> getAllStations() {
        return sections.getAllStations();
    }

    public Station getLineLastDownStation() {
        return sections.getLineLastStation();
    }
}
