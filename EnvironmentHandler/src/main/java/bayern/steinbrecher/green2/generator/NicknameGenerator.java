/*
 * Copyright (C) 2018 Steinbrecher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts {@link String} results in mappings of names to nicknames.
 *
 * @author Stefan Huber
 */
public final class NicknameGenerator {

    /**
     * Prohibit construction.
     */
    private NicknameGenerator() {
        throw new UnsupportedOperationException("Construction of an object is not allowed.");
    }

    /**
     * Generates a mapping from names to nicknames using the given {@code queryResult}.
     *
     * @param queryResult The result to convert.
     * @return The mapping from names to nicknames represented by {@code queryResult }.
     */
    public static Map<String, String> generateNicknames(List<List<String>> queryResult) {
        Map<String, String> mappedNicknames = new HashMap<>();
        queryResult.parallelStream()
                .skip(1) //Skip headings
                .forEach(row -> mappedNicknames.put(row.get(0), row.get(1)));
        return mappedNicknames;
    }
}
