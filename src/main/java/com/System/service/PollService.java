package com.System.service;


import com.System.entity.*;
import com.System.exception.BadRequestException;
import com.System.exception.ResourceNotFoundException;
import com.System.payload.PagedResponse;
import com.System.payload.PollRequest;
import com.System.payload.PollResponse;
import com.System.repository.PollRepository;
import com.System.repository.UserRepository;
import com.System.repository.VoteRepository;
import com.System.security.UserPrincipal;
import com.System.util.AppConstants;
import com.System.util.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PollService {

	private static final Logger logger = LoggerFactory.getLogger(PollService.class);
	@Autowired
	PollRepository pollRepository;

	@Autowired
	VoteRepository voteRepository;

	@Autowired
	UserRepository userRepository;

	/**
	 * @apiNote Create a Poll.
	 * @param pollRequest -(question  ,List<choice> text, PollLength,Hours)
	 * @implSpec Expire the  poll
	 * @return  String Message return
	 */
	public Poll createPoll(PollRequest pollRequest) 
	{
		Poll poll = new Poll();
		poll.setQuestion(pollRequest.getQuestion());

		pollRequest.getChoices()
		.forEach(choiceRequest -> 
		poll.addChoice(new Choice(choiceRequest.getText())));

		Instant now = Instant.now();
		Instant expirationDateTime = now.plus(Duration.ofDays(pollRequest.getPollLength().getDays()))
				.plus(Duration.ofHours(pollRequest.getPollLength().getHours()));

		poll.setExpirationDateTime(expirationDateTime);
		logger.info("Admin Created Poll Successfully");
		return pollRepository.save(poll);
	}

	/**
	 * @param id
	 * @implNote Delete poll by id
	 * @return String Message
	 */
	public void deleteByID(Long id) 
	{
		logger.info("Admin Delete Poll Successfully");
		pollRepository.deleteById(id);
	}

	/**
	 * @param currentUser -current login user
	 * @param pollId - particular Id
	 * @return particular poll getting use in id
	 * @implNote modelMapper class to  response
	 */
	public PollResponse getPollById(Long pollId, UserPrincipal currentUser)
	{
		Poll poll = pollRepository.findById(pollId).orElseThrow(
				() -> new ResourceNotFoundException("Poll", "id", pollId));

		/**
		 *  Retrieve Vote Counts of every choice belonging to the current poll
		 */
		List<ChoiceVoteCount> votes = voteRepository.countByPollIdGroupByChoiceId(pollId);

		Map<Long, Long> choiceVotesMap = votes.stream()
				.collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

		/**
		 *  Retrieve poll creator details
		 */
		User creator = userRepository.findById(poll.getCreatedBy())
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getCreatedBy()));

		/**
		 * Retrieve vote done by logged in user
		 */
		Vote userVote = null;
		if (currentUser != null) {
			userVote = voteRepository.findByUserIdAndPollId(currentUser.getId(), pollId);
		}
		logger.info("Getting particular poll using particular id "+currentUser.getId(), pollId);
		return ModelMapper.mapPollToPollResponse(poll, choiceVotesMap,
				creator, userVote != null ? userVote.getChoice().getId() : null);
	}

	public void validatePageNumberAndSize(int page, int size)
	{
		if (page < 0) {
			throw new BadRequestException("Page number cannot be less than zero.");
		}

		if (size > AppConstants.MAX_PAGE_SIZE) {
			throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
		}
	}

	public Map<Long, Long> getChoiceVoteCountMap(List<Long> pollIds)
	{
		/**
		 *  Retrieve Vote Counts of every Choice belonging to the given pollIds
		 */
		List<ChoiceVoteCount> votes = voteRepository.countByPollIdInGroupByChoiceId(pollIds);

		Map<Long, Long> choiceVotesMap = votes.stream()
				.collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

		return choiceVotesMap;
	}

	public Map<Long, Long> getPollUserVoteMap(UserPrincipal currentUser, List<Long> pollIds) 
	{
		/**
		 *  Retrieve Votes done by the logged in user to the given pollIds
		 */
		Map<Long, Long> pollUserVoteMap = null;
		if(currentUser != null) {
			List<Vote> userVotes = voteRepository.findByUserIdAndPollIdIn(currentUser.getId(), pollIds);

			pollUserVoteMap = userVotes.stream()
					.collect(Collectors.toMap(vote -> vote.getPoll().getId(), vote -> vote.getChoice().getId()));
		}
		return pollUserVoteMap;
	}

	Map<Long, User> getPollCreatorMap(List<Poll> polls)
	{
		/**
		 * Get Poll Creator details of the given list of polls
		 */
		List<Long> creatorIds = polls.stream()
				.map(Poll::getCreatedBy)
				.distinct()
				.collect(Collectors.toList());

		List<User> creators = userRepository.findByIdIn(creatorIds);
		Map<Long, User> creatorMap = creators.stream()
				.collect(Collectors.toMap(User::getId, Function.identity()));

		return creatorMap;
	}

	/**
	 * @param currentUser
	 * @param page
	 * @param size
	 * @return  PagedResponse<>(pollResponses)
	 * @implNote All Polls viewing in Admin only
	 */
	public PagedResponse<PollResponse> getAllPolls(UserPrincipal currentUser, int page, int size) 
	{
		validatePageNumberAndSize(page, size);

		/**
		 *  Retrieve Polls
		 */
		Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		Page<Poll> polls = pollRepository.findAll(pageable);

		if (polls.getNumberOfElements() == 0) {
			return new PagedResponse<>(Collections.emptyList(), polls.getNumber(),
					polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
		}

		/**
		 *  Map Polls to PollResponses containing vote counts and poll creator details
		 */
		List<Long> pollIds = polls.map(Poll::getId).getContent();
		Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
		Map<Long, Long> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);
		Map<Long, User> creatorMap = getPollCreatorMap(polls.getContent());

		List<PollResponse> pollResponses = polls.map(poll -> {
			return ModelMapper.mapPollToPollResponse(poll,
					choiceVoteCountMap,
					creatorMap.get(poll.getCreatedBy()),
					pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null));
		}).getContent();
		logger.info("Getted  All The Poll ");
		return new PagedResponse<>(pollResponses, polls.getNumber(),
				polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
	}
}
