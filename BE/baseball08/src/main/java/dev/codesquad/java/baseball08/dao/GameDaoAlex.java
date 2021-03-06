package dev.codesquad.java.baseball08.dao;

import dev.codesquad.java.baseball08.dto.dto.StageDto;
import dev.codesquad.java.baseball08.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class GameDaoAlex {
    private static final Logger logger = LoggerFactory.getLogger(GameDaoAlex.class);
    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert jdbcInsert;

    @Autowired
    public GameDaoAlex(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // Game ID를 입력받아서 해당 게임에 존재하는 UserID를 가져온다.
    public List<String> getUserIdsByGameId(Long gameId) {
        String sql = "SELECT t.user_id FROM team t WHERE t.game = ?";
        return jdbcTemplate.query(sql, new Object[]{gameId}, (rs, rowNum) -> rs.getString("user_id"));
    }

    // Game ID를 입력받아서 해당 게임에 존재하는 TeamID를 가져온다
    public List<Long> getTeamIdsByGameId(Long gameId) {
        String sql = "SELECT t.id FROM team t WHERE t.game = ?";
        return jdbcTemplate.query(sql, new Object[]{gameId}, (rs, rowNum) -> rs.getLong("id"));
    }

    // BaseBall 게임에 존재하는 전체 Game의 정보를 가져온다
    public List<StageDto> getGameInfo() {
        String sql = "SELECT g.id, GROUP_CONCAT(t.name) AS teams, GROUP_CONCAT(COALESCE(t.user_id,'None')) AS users FROM game g left JOIN team t ON g.id = t.game GROUP BY g.id";
        return jdbcTemplate.query(sql, new Object[]{}, (rs, rowNum) ->
                StageDto.builder()
                        .game(rs.getInt("id"))
                        .away(rs.getString("teams").split(",")[0])
                        .home(rs.getString("teams").split(",")[1])
                        .awayUser(rs.getString("users").split(",")[0])
                        .homeUser(rs.getString("users").split(",")[1])
                        .build());
    }

    // teamId를 입력받아서 gameId를 가져오는 메소드
    public Long getGameIdByTeamId(Long teamId) {
        String sql = "SELECT t.game FROM team t WHERE t.id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{teamId}, (rs, rowNum) -> rs.getLong("game"));
    }

    public void saveInning(Inning inning) {
        String sql = "INSERT INTO inning(home_name,away_name,game,game_key) " +
                "VALUES (?,?,?,?)";
        String[] param = new String[]{"id"};
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement statement = con.prepareStatement(sql, param);
            statement.setString(1, inning.getHomeName());
            statement.setString(2, inning.getAwayName());
            statement.setLong(3, inning.getGame());
            statement.setInt(4, inning.getGame_key());
            return statement;
        }, keyHolder);
    }

    public int getGameKeyForInning(Long game) {
        String sql = "SELECT COUNT(i.id) AS inning_count FROM inning i WHERE i.game = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{game}, (rs, rowNum) -> rs.getInt("inning_count"));
    }

    public List<String> getTeamNamesByGameId(Long gameId) {
        String sql = "SELECT t.name FROM team t WHERE t.game = ?";
        return jdbcTemplate.query(sql, new Object[]{gameId}, (rs, rowNum) -> rs.getString("name"));
    }

    public Inning getPresentInning(Long gameId) {
        String sql = "SELECT * FROM inning WHERE inning.game_key = (SELECT MAX(game_key) FROM inning) AND inning.game = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{gameId},
                (rs, rowNum) -> Inning.builder()
                        .awayName(rs.getString("away_name"))
                        .homeName(rs.getString("home_name"))
                        .topBottom(rs.getBoolean("top_bottom"))
                        .awayScore(rs.getInt("away_score"))
                        .homeScore(rs.getInt("away_score"))
                        .strikeCount(rs.getInt("strike_count"))
                        .ballCount(rs.getInt("ball_count"))
                        .outCount(rs.getInt("out_count"))
                        .baseCount(rs.getInt("base_count"))
                        .game(rs.getLong("game"))
                        .game_key(rs.getInt("game_key"))
                        .build());
    }

    public void updateCurrentInning(Long id, Integer currentInning) {
        String sql = "UPDATE game g SET current_inning = ? WHERE g.id = ?";
        this.jdbcTemplate.update(sql, new Object[] {currentInning, id});
    }

}
