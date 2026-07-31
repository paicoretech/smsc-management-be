package com.smsc.management.app.routing.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.paicbd.smsc.utils.GeneralSmscConstants;
import com.smsc.management.app.routing.dto.CustomParamMatcherDTO;
import com.smsc.management.app.routing.dto.RoutingRulesActionAdvancedDTO;
import com.smsc.management.app.routing.model.entity.CustomParamMatcher;
import com.smsc.management.app.routing.model.entity.RoutingRulesActionAdvanced;
import com.smsc.management.app.routing.model.repository.CustomParamMatcherRepository;
import com.smsc.management.app.routing.model.repository.RoutingRulesActionAdvancedRepository;
import com.smsc.management.app.user.model.entity.Users;
import com.smsc.management.app.user.model.repository.UserRepository;
import com.smsc.management.app.user.model.repository.UserServiceProviderRepository;
import com.smsc.management.app.user.utils.UtilsUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.smsc.management.app.routing.dto.NetworksToRoutingRulesDTO;
import com.smsc.management.app.routing.dto.RedisRoutingRulesDTO;
import com.smsc.management.utils.ApiResponse;
import com.smsc.management.app.routing.dto.RoutingRulesDTO;
import com.smsc.management.app.routing.dto.RoutingRulesDestinationDTO;
import com.smsc.management.app.routing.model.entity.RoutingRules;
import com.smsc.management.app.routing.model.entity.RoutingRulesDestination;
import com.smsc.management.app.sequence.SequenceNetworksId;
import com.smsc.management.exception.InvalidStructureException;
import com.smsc.management.app.routing.mapper.RoutingRulesMapper;
import com.smsc.management.app.routing.model.repository.RoutingRulesDestinationRepository;
import com.smsc.management.app.routing.model.repository.RoutingRulesRepository;
import com.smsc.management.app.sequence.SequenceNetworksIdRepository;
import com.smsc.management.utils.Constants;
import com.smsc.management.utils.ResponseMapping;
import com.smsc.management.utils.UtilsBase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for processing routing rules and their destinations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingRulesService {
    
    private final RoutingRulesRepository routingRuleRepo;
    private final RoutingRulesDestinationRepository routingRulesDestinationRepo;
    private final SequenceNetworksIdRepository sequenceNetworkRepo;
    private final CustomParamMatcherRepository customParamMatcherRepo;
    private final RoutingRulesActionAdvancedRepository rulesActionAdvancedRepo;
    private final UserRepository userRepository;
    private final UserServiceProviderRepository userServiceProviderRepository;
    private final RoutingRulesMapper routingRulesMapper;
    private final UtilsBase utilsBase;

    /**
     * Retrieves all routing rules along with their destinations.
     *
     * @return A ResponseDTO containing the list of routing rules and their destinations.
     */
    public ApiResponse getRoutingRules() {
        try {
            List<RoutingRulesDTO> routingRulesDTOs = routingRuleRepo.findByRoutingRulesList();

            routingRulesDTOs.forEach(dto -> {
                List<RoutingRulesDestinationDTO> destinationsDTO = routingRulesDestinationRepo.findByRoutingRulesIdList(dto.getId());
                List<CustomParamMatcherDTO> customParamMatcherDTOS = customParamMatcherRepo.findAllByRoutingRuleId(dto.getId());
                dto.setDestination(destinationsDTO);
                dto.setCustomParamMatcher(customParamMatcherDTOS);

                if (UtilsUser.hasPermissionForActionsAdvanced()) {
                    RoutingRulesActionAdvanced routingRulesActionAdvanced = rulesActionAdvancedRepo.findFirstByRoutingRulesId(dto.getId());
                    dto.setActionAdvanced(routingRulesMapper.toDtoActionAdvanced(routingRulesActionAdvanced));
                }
            });

            return ResponseMapping.successMessage("get routing rules request success", routingRulesDTOs);
        } catch (Exception e) {
            log.error("Get routing rules request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Get routing rules request with error", e);
        }
    }

    /**
     * Creates a new routing rule along with its destinations.
     *
     * @param newRouting The new routing rule to be created.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
    @Transactional
    public ApiResponse create(RoutingRulesDTO newRouting) {
        int rulesId = 0;
        try {
            RoutingRules routingRules = new RoutingRules();
            List<RoutingRulesDestinationDTO> destinations = newRouting.getDestination();

            if (destinations.isEmpty()) {
                throw new InvalidStructureException("At least one destination is required.");
            }
            utilsBase.validateDestinationRules(destinations);
            utilsBase.validateNetworksParameter(newRouting);

            routingRulesMapper.updateEntityFromDTO(newRouting, routingRules);
            var resultInserted = routingRuleRepo.save(routingRules);
            rulesId = resultInserted.getId();

            this.createDestinationRule(destinations, rulesId);
            boolean addActionsAdvanced = UtilsUser.hasPermissionForActionsAdvanced();
            if (addActionsAdvanced) {
                this.processRuleActionAdvanced(rulesId, newRouting.getActionAdvanced(), true, false);
            }

            List<CustomParamMatcherDTO> customParamMatcherList = newRouting.getCustomParamMatcher();
            if (Objects.nonNull(customParamMatcherList) && !customParamMatcherList.isEmpty()) {
                utilsBase.validateCustomParameter(customParamMatcherList);
                this.createCustomParameter(customParamMatcherList, rulesId);
            }

            // prepare data response
            destinations = routingRulesDestinationRepo.findByRoutingRulesIdList(rulesId);
            customParamMatcherList = customParamMatcherRepo.findAllByRoutingRuleId(rulesId);
            newRouting.setDestination(destinations);
            newRouting.setCustomParamMatcher(customParamMatcherList);
            newRouting.setId(rulesId);

            if (this.socketAndRedisAction(newRouting.getOriginNetworkId())) {
                log.info("Routing rules stored in Redis and socket success -> {}", newRouting);
            }

            return ResponseMapping.successMessage("Routing rule added successfully.", newRouting);
        } catch (DataIntegrityViolationException e) {
            if (rulesId > 0) {
                manualRollback(rulesId);
            }
            log.error("New routing rules request with DataIntegrityViolationException: {}", e.getMessage());
            throw e;
        } catch (InvalidStructureException e) {
            log.error("New routing rules request with InvalidStructureException: {}", e.getMessage());
            return e.exceptionMessage("New routing rules request with error", e);
        } catch (Exception e) {
            log.error("New routing rules request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("New routing rules request with error", e);
        }
    }

    /**
     * Updates an existing routing rule along with its destinations.
     *
     * @param id              The ID of the routing rule to be updated.
     * @param routingRulesDTO The updated routing rule information.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
    @Transactional
    public ApiResponse update(int id, RoutingRulesDTO routingRulesDTO) {
        try {
            // validate destinations rules
            List<RoutingRulesDestinationDTO> destinations = routingRulesDTO.getDestination();
            if (destinations.isEmpty()) {
                throw new InvalidStructureException("At least one destination is required.");
            }
            utilsBase.validateDestinationRules(destinations);
            utilsBase.validateNetworksParameter(routingRulesDTO);

            // finding routing rule
            RoutingRules routingRules = routingRuleRepo.findById(id);
            if (Objects.isNull(routingRules)) {
                return ResponseMapping.errorMessageNoFound("Routing rules with ID= " + id + " was not found.");
            }
            int previousNetworkId = routingRules.getOriginNetworkId();

            this.updateDestinationRule(destinations, id);
            boolean addActionsAdvanced = UtilsUser.hasPermissionForActionsAdvanced();
            if (addActionsAdvanced) {
                this.processRuleActionAdvanced(id, routingRulesDTO.getActionAdvanced(), true, true);
            }

            customParamMatcherRepo.deleteAllByRoutingRuleId(id);
            List<CustomParamMatcherDTO> customParamMatcher = routingRulesDTO.getCustomParamMatcher();
            if (Objects.nonNull(customParamMatcher) && !customParamMatcher.isEmpty()) {
                utilsBase.validateCustomParameter(customParamMatcher);
                this.createCustomParameter(customParamMatcher, id);
            }

            routingRulesDTO.setId(id);
            routingRulesMapper.updateEntityFromDTO(routingRulesDTO, routingRules);
            var resultRoutingRule = routingRuleRepo.save(routingRules);

            // mapping dto response
            routingRulesMapper.DTOfromEntity(resultRoutingRule, routingRulesDTO);
            List<RoutingRulesDestinationDTO> destinationsDTO = routingRulesDestinationRepo.findByRoutingRulesIdList(id);
            customParamMatcher = customParamMatcherRepo.findAllByRoutingRuleId(id);
            routingRulesDTO.setDestination(destinationsDTO);
            routingRulesDTO.setCustomParamMatcher(customParamMatcher);

            if (this.socketAndRedisAction(routingRulesDTO.getOriginNetworkId())) {
                log.info("Routing rules updated in Redis and socket success -> {}", routingRulesDTO);

                if (previousNetworkId != routingRulesDTO.getOriginNetworkId() && this.socketAndRedisAction(previousNetworkId)) {
                    log.info("Routing rules updated in Redis and socket success with previous originNetworkId = {}", previousNetworkId);
                }
            }

            return ResponseMapping.successMessage("Routing rule updated successfully.", routingRulesDTO);
        } catch (DataIntegrityViolationException e) {
            log.error("Routing rules request to update with DataIntegrityViolationException: {}", e.getMessage());
            throw e;
        } catch (InvalidStructureException e) {
            log.error("Routing rules request to update with InvalidStructureException: {}", e.getMessage());
            return e.exceptionMessage("Routing rules request to update with error", e);
        } catch (Exception e) {
            log.error("Routing rules request with error: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Routing rules request with error", e);
        }
    }

    /**
     * Deletes a routing rule by its ID along with its destinations.
     *
     * @param id The ID of the routing rule to be deleted.
     * @return A ResponseDTO indicating the success or failure of the operation.
     */
    @Transactional
    public ApiResponse delete(int id) {
        try {
            RoutingRules routingRule = routingRuleRepo.findById(id);
            if (routingRule != null) {
                // routing rules data to response
                List<RoutingRulesDestinationDTO> routingRulesDestinationDTO = routingRulesDestinationRepo.findByRoutingRulesIdList(id);
                RoutingRulesDTO routingRulesDTO = new RoutingRulesDTO();
                routingRulesMapper.DTOfromEntity(routingRule, routingRulesDTO);
                routingRulesDTO.setDestination(routingRulesDestinationDTO);

                // deleting data
                List<RoutingRulesDestination> routingRuleDestinationDeleted = routingRulesDestinationRepo.findByRoutingRulesId(id);
                routingRulesDestinationRepo.deleteAll(routingRuleDestinationDeleted);
                this.processRuleActionAdvanced(id, null, false, true);
                customParamMatcherRepo.deleteAllByRoutingRuleId(id);
                routingRuleRepo.delete(routingRule);

                if (this.socketAndRedisAction(routingRule.getOriginNetworkId())) {
                    log.info("Routing rules deleted in Redis and socket success -> {}", routingRulesDTO);
                }

                return ResponseMapping.successMessage("Routing rules deleted successful.", routingRulesDTO);
            }

            return ResponseMapping.errorMessageNoFound("Routing rules was not found.");
        } catch (Exception e) {
            log.error("Routing rules request with error in delete(): {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Routing rules request was end with error", e);
        }
    }

    public ApiResponse updateRedisAndSocketNotification(int originNetworkId) {
        try {
            this.socketAndRedisAction(originNetworkId);
            return ResponseMapping.successMessage("update redis and notification socket successful.", null);
        } catch (Exception e) {
            log.error("Error to update: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Error to update", e);
        }
    }

    private void createCustomParameter(List<CustomParamMatcherDTO> customParams, int routingRuleId) {
        List<CustomParamMatcher> customParamMatcherList = new ArrayList<>();
        for (CustomParamMatcherDTO customParam : customParams) {
            CustomParamMatcher customParamMatcher = new CustomParamMatcher();
            customParamMatcher.setPropertyName(customParam.getPropertyName());
            customParamMatcher.setValueMatcher(customParam.getValueMatcher());
            customParamMatcher.setRoutingRuleId(routingRuleId);

            customParamMatcherList.add(customParamMatcher);
        }

        customParamMatcherRepo.saveAll(customParamMatcherList);
    }

    private void createDestinationRule(List<RoutingRulesDestinationDTO> destinations, int rulesId) {
        List<RoutingRulesDestination> routingRulesDestinationArray = new ArrayList<>();

        for (RoutingRulesDestinationDTO routing : destinations) {
            RoutingRulesDestination routingRulesDestination = new RoutingRulesDestination();
            SequenceNetworksId seqNet = sequenceNetworkRepo.findById(routing.getNetworkId());

            routingRulesDestination.setNetworkId(routing.getNetworkId());
            routingRulesDestination.setPriority(routing.getPriority());
            routingRulesDestination.setRoutingRulesId(rulesId);
            routingRulesDestination.setNetworkType(seqNet.getNetworkType());

            routingRulesDestinationArray.add(routingRulesDestination);
        }
        routingRulesDestinationRepo.saveAll(routingRulesDestinationArray);
    }

    private void updateDestinationRule(List<RoutingRulesDestinationDTO> destinations, int rulesId) throws InvalidStructureException {
        for (RoutingRulesDestinationDTO destination : destinations) {
            RoutingRulesDestination routingRulesDestination = new RoutingRulesDestination();
            SequenceNetworksId seqNet = sequenceNetworkRepo.findById(destination.getNetworkId());

            if (destination.getAction() != 2) {
                routingRulesDestination = routingRulesDestinationRepo.findById(destination.getId());
                if (destination.getAction() == 0) {
                    routingRulesDestination.setPriority(destination.getPriority());
                }
            } else {
                routingRulesDestination.setPriority(destination.getPriority());
                routingRulesDestination.setNetworkId(destination.getNetworkId());
                routingRulesDestination.setNetworkType(seqNet.getNetworkType());
            }

            switch (destination.getAction()) {
                case 0:
                    routingRulesDestinationRepo.save(routingRulesDestination);
                    break;
                case 1:
                    routingRulesDestinationRepo.delete(routingRulesDestination);
                    break;
                case 2:
                    routingRulesDestination.setRoutingRulesId(rulesId);
                    routingRulesDestinationRepo.save(routingRulesDestination);
                    break;
                default:
                    throw new InvalidStructureException("Only values 0 (update), 1 (delete) and 2 (new) are allowed in the action field");
            }
        }
    }

    /**
     * Performs a manual rollback of transactions in case of an error during the creation of a routing rule.
     *
     * @param routingRulesId The ID of the routing rule to perform the rollback.
     */
    @Transactional
    public void manualRollback(int routingRulesId) {
        try {
            log.info("applying rollback to routing_rules_destination table");
            List<RoutingRulesDestination> routingDestinations = routingRulesDestinationRepo.findByRoutingRulesId(routingRulesId);
            routingRulesDestinationRepo.deleteAll(routingDestinations);

            log.info("applying rollback to custom_param_matcher table");
            customParamMatcherRepo.deleteAllByRoutingRuleId(routingRulesId);

            log.info("applying rollback to actionAdvanced table");
            rulesActionAdvancedRepo.deleteByRoutingRulesId(routingRulesId);

            log.info("applying rollback to routing_rules table");
            RoutingRules routingRule = routingRuleRepo.findById(routingRulesId);
            routingRuleRepo.delete(routingRule);
        } catch (Exception e) {
            log.error("applying rollback ended with error: {}", e.getMessage());
        }
    }

    public ApiResponse getNetworks() {
        try {
            List<NetworksToRoutingRulesDTO> networks = routingRuleRepo.findGatewayNamesAndIds();

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAuthenticatedUser = Objects.nonNull(auth) && auth.isAuthenticated();

            if (!isAuthenticatedUser) {
                log.warn("Unauthorized access attempt to networks - no valid authentication found");
                return ResponseMapping.errorMessage("Authentication required to access this resource");
            }

            String username = auth.getName();
            Users currentUser = userRepository.findByUserName(username).orElse(null);

            if (currentUser == null) {
                log.warn("Unauthorized access attempt - user not found: {}", username);
                return ResponseMapping.errorMessage("User not found");
            }

            boolean isAdmin = currentUser.getRoles().stream()
                    .anyMatch(role -> role.equals("ROOT") || role.equals("ADMINISTRATOR"));
            boolean hasAllServiceProvidersAccess = currentUser.isAllServiceProviders();
            boolean needsFiltering = !isAdmin && !hasAllServiceProvidersAccess;

            if (needsFiltering) {
                List<Integer> assignedProviderIds = userServiceProviderRepository
                        .findByUserId(currentUser.getId())
                        .stream()
                        .map(usp -> usp.getServiceProvider().getNetworkId())
                        .toList();

                networks = networks.stream()
                        .filter(network -> {
                            boolean isNotServiceProvider = !"sp".equals(network.getType());
                            boolean hasAssignedServiceProvider = assignedProviderIds.contains(network.getNetworkId());
                            return isNotServiceProvider || hasAssignedServiceProvider;
                        })
                        .collect(Collectors.toList());
            }

            return ResponseMapping.successMessage("Get networks successful", networks);
        } catch (Exception e) {
            log.error("Error to get networks list: {}", e.getMessage());
            return ResponseMapping.exceptionMessage("Error to get networks", e);
        }
    }

    public boolean socketAndRedisAction(int originNetWorkId) {
        try {
            String originProtocol = routingRuleRepo.findOriginProtocol(originNetWorkId);
            List<RedisRoutingRulesDTO> routingRules = utilsBase.getRoutingRules(originNetWorkId, originProtocol);

            if (routingRules.isEmpty()) {
                utilsBase.removeInRedis(GeneralSmscConstants.ROUTING_RULES_HASH_NAME, Integer.toString(originNetWorkId));
                utilsBase.sendNotificationSocket(Constants.DELETE_ROUTING_RULES_ENDPOINT, Integer.toString(originNetWorkId));
                return true;
            }

            utilsBase.storeInRedis(GeneralSmscConstants.ROUTING_RULES_HASH_NAME, Integer.toString(originNetWorkId), routingRules.toString());
            utilsBase.sendNotificationSocket(Constants.UPDATE_ROUTING_RULES_ENDPOINT, Integer.toString(originNetWorkId));

            return true;
        } catch (Exception e) {
            log.error("Error to create object in Redis and sent socket notification -> {}", e.getMessage());
        }
        return false;
    }

    private void processRuleActionAdvanced(int routingRuleId, RoutingRulesActionAdvancedDTO routingRulesActionAdvancedDTO, boolean create, boolean delete) {
        if (delete) {
            rulesActionAdvancedRepo.deleteByRoutingRulesId(routingRuleId);
        }

        if (create) {
            this.validateOperationCodeSRI(routingRulesActionAdvancedDTO.getOperationCodeSri());
            this.validateOperationCodeMT(routingRulesActionAdvancedDTO.getOperationCodeMt());
            RoutingRulesActionAdvanced actionAdvanced = routingRulesMapper.toEntityActionAdvanced(routingRulesActionAdvancedDTO);
            actionAdvanced.setRoutingRulesId(routingRuleId);
            rulesActionAdvancedRepo.save(actionAdvanced);
        }
    }

    private void validateOperationCodeSRI(int operationCodeSri) {
        if (operationCodeSri != 45 && operationCodeSri != 22 && operationCodeSri != -1) {
            throw new IllegalArgumentException("operationCodeSri should be 45(SRI_SM) or 22(SRI Voice)");
        }
    }

    private void validateOperationCodeMT(Integer operationCodeMT) {
        if (operationCodeMT != 44 && operationCodeMT != 46 && operationCodeMT != -1) {
            throw new IllegalArgumentException("operation code for MT-FSM should be 44 or 46");
        }
    }
}
