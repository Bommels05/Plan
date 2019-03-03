/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.db.access.queries.objects;

import com.djrapitops.plan.data.time.GMTimes;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.tables.WorldTable;
import com.djrapitops.plan.db.sql.tables.WorldTimesTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.djrapitops.plan.db.sql.parsing.Sql.*;

/**
 * Queries for {@link com.djrapitops.plan.data.time.WorldTimes} objects.
 *
 * @author Rsl1122
 */
public class WorldTimesQueries {

    private static String worldColumn = "world";
    private static final String SELECT_WORLD_TIMES_STATEMENT_START = SELECT +
            "SUM(" + WorldTimesTable.SURVIVAL + ") as survival, " +
            "SUM(" + WorldTimesTable.CREATIVE + ") as creative, " +
            "SUM(" + WorldTimesTable.ADVENTURE + ") as adventure, " +
            "SUM(" + WorldTimesTable.SPECTATOR + ") as spectator, " +
            WorldTimesTable.TABLE_NAME + "." + WorldTimesTable.SERVER_UUID + ", " +
            WorldTable.TABLE_NAME + "." + WorldTable.NAME + " as " + worldColumn +
            FROM + WorldTimesTable.TABLE_NAME +
            " INNER JOIN " + WorldTable.TABLE_NAME + " on " + WorldTable.TABLE_NAME + "." + WorldTable.ID + "=" + WorldTimesTable.WORLD_ID;

    private WorldTimesQueries() {
        /* Static method class */
    }

    /**
     * Sum total playtime per world on a server.
     *
     * @param serverUUID Server UUID of the Plan server.
     * @return WorldTimes with world name - playtime ms information.
     */
    public static Query<WorldTimes> fetchServerTotalWorldTimes(UUID serverUUID) {
        String sql = SELECT_WORLD_TIMES_STATEMENT_START +
                WHERE + WorldTimesTable.TABLE_NAME + "." + WorldTimesTable.SERVER_UUID + "=?" +
                GROUP_BY + worldColumn;

        return new QueryStatement<WorldTimes>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public WorldTimes processResults(ResultSet set) throws SQLException {
                String[] gms = GMTimes.getGMKeyArray();

                WorldTimes worldTimes = new WorldTimes(new HashMap<>());
                while (set.next()) {
                    String worldName = set.getString(worldColumn);

                    GMTimes gmTimes = extractGMTimes(set, gms);

                    worldTimes.setGMTimesForWorld(worldName, gmTimes);
                }
                return worldTimes;
            }
        };
    }

    /**
     * Sum total playtime per world on all servers.
     *
     * @param playerUUID UUID of the player.
     * @return WorldTimes with world name - playtime ms information.
     */
    public static Query<WorldTimes> fetchPlayerTotalWorldTimes(UUID playerUUID) {
        String sql = SELECT_WORLD_TIMES_STATEMENT_START +
                WHERE + WorldTimesTable.USER_UUID + "=?" +
                GROUP_BY + worldColumn;

        return new QueryStatement<WorldTimes>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public WorldTimes processResults(ResultSet set) throws SQLException {
                String[] gms = GMTimes.getGMKeyArray();

                WorldTimes worldTimes = new WorldTimes(new HashMap<>());
                while (set.next()) {
                    String worldName = set.getString(worldColumn);

                    GMTimes gmTimes = extractGMTimes(set, gms);

                    worldTimes.setGMTimesForWorld(worldName, gmTimes);
                }
                return worldTimes;
            }
        };
    }

    /**
     * Find total world times of the player on servers.
     *
     * @param playerUUID UUID of the player.
     * @return Map: Server UUID - WorldTimes total for the server
     */
    public static Query<Map<UUID, WorldTimes>> fetchPlayerWorldTimesOnServers(UUID playerUUID) {
        String sql = SELECT_WORLD_TIMES_STATEMENT_START +
                WHERE + WorldTimesTable.TABLE_NAME + "." + WorldTimesTable.USER_UUID + "=?" +
                GROUP_BY + worldColumn + ", " + WorldTimesTable.TABLE_NAME + "." + WorldTimesTable.SERVER_UUID;

        return new QueryStatement<Map<UUID, WorldTimes>>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Map<UUID, WorldTimes> processResults(ResultSet set) throws SQLException {
                String[] gms = GMTimes.getGMKeyArray();

                Map<UUID, WorldTimes> worldTimesMap = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(WorldTimesTable.SERVER_UUID));
                    WorldTimes worldTimes = worldTimesMap.getOrDefault(serverUUID, new WorldTimes(new HashMap<>()));
                    String worldName = set.getString(worldColumn);

                    GMTimes gmTimes = extractGMTimes(set, gms);

                    worldTimes.setGMTimesForWorld(worldName, gmTimes);
                    worldTimesMap.put(serverUUID, worldTimes);
                }
                return worldTimesMap;
            }
        };
    }

    private static GMTimes extractGMTimes(ResultSet set, String[] gms) throws SQLException {
        Map<String, Long> gmMap = new HashMap<>();
        for (String gameMode : gms) {
            gmMap.put(gameMode, set.getLong(gameMode));
        }
        return new GMTimes(gmMap);
    }
}