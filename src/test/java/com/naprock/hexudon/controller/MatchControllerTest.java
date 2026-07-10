//package com.naprock.hexudon.controller;
//
//import com.naprock.hexudon.adapter.in.rest.MatchController;
//import com.naprock.hexudon.domain.model.Team;
//import com.naprock.hexudon.domain.valueobject.*;
//import com.naprock.hexudon.manager.MatchManager;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.hamcrest.Matchers.hasSize;
//import static org.hamcrest.Matchers.is;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(MatchController.class)
//class MatchControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private MatchManager matchManager;
//
//    @Test
//    void testGetMatchState() throws Exception {
//        MatchState mockState = new MatchState();
//        mockState.setStatus(MatchStatus.WAITING);
//        mockState.setCurrentTurn(0);
//        mockState.setTeams(new ArrayList<>());
//
//        Mockito.when(matchManager.getMatchState()).thenReturn(mockState);
//
//        mockMvc.perform(get("/api/match/state"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.status", is("WAITING")))
//                .andExpect(jsonPath("$.currentTurn", is(0)))
//                .andExpect(jsonPath("$.teams").isArray())
//                .andExpect(jsonPath("$.teams", hasSize(0)));
//
//        Mockito.verify(matchManager, Mockito.times(1)).getMatchState();
//    }
//
//    @Test
//    void testPostRegister() throws Exception {
//        Team mockTeam = new Team("test-team");
//        Mockito.when(matchManager.registerTeam(anyString())).thenReturn(mockTeam);
//
//        mockMvc.perform(post("/api/match/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"teamName\":\"test-team\"}"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.teamName", is("test-team")))
//                .andExpect(jsonPath("$.agents").isArray())
//                .andExpect(jsonPath("$.agents", hasSize(0)));
//
//        Mockito.verify(matchManager, Mockito.times(1)).registerTeam("test-team");
//    }
//
//    @Test
//    void testPostStart() throws Exception {
//        MatchState mockState = new MatchState();
//        mockState.setStatus(MatchStatus.PLAYING);
//        mockState.setCurrentTurn(1);
//
//        Mockito.when(matchManager.getMatchState()).thenReturn(mockState);
//
//        mockMvc.perform(post("/api/match/start"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.status", is("PLAYING")))
//                .andExpect(jsonPath("$.currentTurn", is(1)));
//
//        Mockito.verify(matchManager, Mockito.times(1)).startMatch();
//        Mockito.verify(matchManager, Mockito.times(1)).getMatchState();
//    }
//
//    @Test
//    void testPostSubmitActions() throws Exception {
//        List<Action> actionsList = List.of(new Action(1, ActionType.WAIT, null, null, 123456789L));
//        AgentExecutionResult execResult = new AgentExecutionResult("A1", actionsList);
//        TurnSimulationResult mockResult = new TurnSimulationResult(1, List.of(execResult));
//
//        Mockito.when(matchManager.submitActions(
//                Mockito.eq("test-team"),
//                Mockito.eq(1),
//                Mockito.anyMap()
//        )).thenReturn(mockResult);
//
//        mockMvc.perform(post("/api/match/actions")
//                        .header("X-Team-Name", "test-team")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"day\":1,\"agentPlans\":[{\"agentId\":\"A1\",\"actions\":[{\"order\":1,\"actionType\":\"WAIT\"}]}]}"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.day", is(1)))
//                .andExpect(jsonPath("$.agentPlans").isArray())
//                .andExpect(jsonPath("$.agentPlans", hasSize(1)))
//                .andExpect(jsonPath("$.agentPlans[0].agentId", is("A1")))
//                .andExpect(jsonPath("$.agentPlans[0].actions").isArray())
//                .andExpect(jsonPath("$.agentPlans[0].actions", hasSize(1)))
//                .andExpect(jsonPath("$.agentPlans[0].actions[0].order", is(1)))
//                .andExpect(jsonPath("$.agentPlans[0].actions[0].actionType", is("WAIT")));
//
//        Mockito.verify(matchManager, Mockito.times(1)).submitActions(
//                Mockito.eq("test-team"), Mockito.eq(1), Mockito.anyMap()
//        );
//    }
//}
