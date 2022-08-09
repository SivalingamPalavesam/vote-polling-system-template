package com.System.service;

import com.System.entity.Poll;
import com.System.entity.User;
import com.System.exception.ResourceNotFoundException;
import com.System.payload.PagedResponse;
import com.System.payload.PollResponse;
import com.System.repository.PollRepository;
import com.System.repository.UserRepository;
import com.System.repository.VoteRepository;
import com.System.security.UserPrincipal;
import com.System.util.ModelMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {


    @Autowired
    UserRepository userRepository;

    @Autowired
    VoteRepository voteRepository;

    @Autowired
    PollRepository pollRepository;

    @Autowired
    PollService pollService;

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    
    /**
	 * @param username
	 * @param currentUser
	 * @param page
	 * @param size
	 * @return PagedResponse(pollResponses,Number(),Size(),TotalElements(), TotalPages(), isLast())
	 * @implSpec already pollResponses ==> all polls value stored
	 */
    public PagedResponse<PollResponse> getPollsVotedBy(String username, UserPrincipal currentUser, int page, int size) {
       
    	pollService.validatePageNumberAndSize(page, size);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        /**
         * Retrieve all pollIds in which the given username has voted
         */
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Long> userVotedPollIds = voteRepository.findVotedPollIdsByUserId(user.getId(), pageable);

        if (userVotedPollIds.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), userVotedPollIds.getNumber(),
                    userVotedPollIds.getSize(), userVotedPollIds.getTotalElements(),
                    userVotedPollIds.getTotalPages(), userVotedPollIds.isLast());
        }

        /**
         *  Retrieve all poll details from the voted pollIds.
         */
        List<Long> pollIds = userVotedPollIds.getContent();

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<Poll> polls = pollRepository.findByIdIn(pollIds, sort);

        /**
         * Map Polls to PollResponses containing vote counts and poll creator details
         */
        Map<Long, Long> choiceVoteCountMap = pollService.getChoiceVoteCountMap(pollIds);
        Map<Long, Long> pollUserVoteMap = pollService.getPollUserVoteMap(currentUser, pollIds);
        Map<Long, User> creatorMap = pollService.getPollCreatorMap(polls);

        List<PollResponse> pollResponses = polls.stream().map(poll -> {
            return ModelMapper.mapPollToPollResponse(poll,
                    choiceVoteCountMap,
                    creatorMap.get(poll.getCreatedBy()),
                    pollUserVoteMap == null ? null : pollUserVoteMap.getOrDefault(poll.getId(), null));
        }).collect(Collectors.toList());
        
        logger.info("Admin Viewing The User List And Voter List");
        return new PagedResponse<>(pollResponses, userVotedPollIds.getNumber(), userVotedPollIds.getSize(), userVotedPollIds.getTotalElements(), userVotedPollIds.getTotalPages(), userVotedPollIds.isLast());
    }
}
