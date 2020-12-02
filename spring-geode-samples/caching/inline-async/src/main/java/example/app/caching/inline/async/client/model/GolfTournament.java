/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package example.app.caching.inline.async.client.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.util.Assert;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Abstract Data Type (ADT) modeling a golf tournament.
 *
 * @author John Blum
 * @since 1.4.0
 */
@Getter
@ToString
@RequiredArgsConstructor(staticName = "newGolfTournament")
@SuppressWarnings("unused")
public class GolfTournament implements Iterable<GolfTournament.Pairing> {

	@NonNull
	private final String name;

	private GolfCourse golfCourse;

	private final List<Pairing> pairings = new ArrayList<>();

	private final Set<Golfer> players = new HashSet<>();

	@Override
	public Iterator<GolfTournament.Pairing> iterator() {
		return Collections.unmodifiableList(this.pairings).iterator();
	}

	public boolean isFinished() {

		Set<Pairing> finishedPairings = new HashSet<>(this.pairings.size());

		for (Pairing pairing : this) {
			if (pairing.getHole() < 18) {
				return false;
			}
			else {
				finishedPairings.add(pairing);
			}
		}

		this.pairings.removeAll(finishedPairings);

		return this.pairings.isEmpty();
	}

	public GolfTournament at(GolfCourse golfCourse) {

		Assert.notNull(golfCourse, "Golf Course must not be null");

		this.golfCourse = golfCourse;

		return this;
	}

	public GolfTournament buildPairings() {

		Assert.notEmpty(this.players,
			() -> String.format("No players are registered for this golf tournament [%s]", getName()));

		Assert.isTrue(this.players.size() % 2 == 0,
			() -> String.format("An even number of players must register to play this golf tournament [%s]; currently at [%d]",
				getName(), this.players.size()));

		List<Golfer> playersToPair = new ArrayList<>(this.players);

		Collections.shuffle(playersToPair);

		for (int index = 0, size = playersToPair.size(); index < size; index += 2) {
			this.pairings.add(Pairing.of(playersToPair.get(index), playersToPair.get(index + 1)));
		}

		return this;
	}

	public GolfTournament play() {

		Assert.state(this.golfCourse != null, "No golf course was declared");
		Assert.state(!this.players.isEmpty(), "Golfers must register to play before the golf tournament is played");
		Assert.state(!this.pairings.isEmpty(), "Pairings must be formed before the golf tournament is played");
		Assert.state(!isFinished(), () -> String.format("Golf tournament [%s] has already been played", getName()));

		return this;
	}

	public GolfTournament register(Golfer... players) {
		return register(Arrays.asList(ArrayUtils.nullSafeArray(players, Golfer.class)));
	}

	public GolfTournament register(Iterable<Golfer> players) {

		StreamSupport.stream(CollectionUtils.nullSafeIterable(players).spliterator(), false)
			.filter(Objects::nonNull)
			.forEach(this.players::add);

		return this;
	}

	@Getter
	@ToString
	@RequiredArgsConstructor(staticName = "of")
	public static class Pairing {

		@NonNull
		private final Golfer playerOne;

		@NonNull
		private final Golfer playerTwo;

		public int getHole() {
			return getPlayerOne().getHole();
		}

		public void setHole(int hole) {
			this.playerOne.setHole(hole);
			this.playerTwo.setHole(hole);
		}

		public int playNextHole() {
			return getHole() + 1;
		}
	}
}
