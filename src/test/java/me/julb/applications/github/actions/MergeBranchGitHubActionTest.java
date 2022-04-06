/**
 * MIT License
 *
 * Copyright (c) 2017-2022 Julb
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.julb.applications.github.actions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import me.julb.sdk.github.actions.kit.GitHubActionsKit;

/**
 * Test class for {@link MergeBranchGitHubAction} class. <br>
 * @author Julb.
 */
@ExtendWith(MockitoExtension.class)
class MergeBranchGitHubActionTest {

    /**
     * The class under test.
     */
    private MergeBranchGitHubAction githubAction = null;

    /**
     * A mock for GitHub action kit.
     */
    @Mock
    private GitHubActionsKit ghActionsKitMock;

    /**
     * A mock for GitHub API.
     */
    @Mock
    private GitHub ghApiMock;

    /**
     * A mock for GitHub repository.
     */
    @Mock
    private GHRepository ghRepositoryMock;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception {
        githubAction = new MergeBranchGitHubAction();
        githubAction.setGhActionsKit(ghActionsKitMock);
        githubAction.setGhApi(ghApiMock);
        githubAction.setGhRepository(ghRepositoryMock);
    }

    /**
     * Test method.
     */
    @Test
    void whenGetInputTo_thenReturnValue() throws Exception {
        when(this.ghActionsKitMock.getRequiredInput("to")).thenReturn("branch-to");

        assertThat(this.githubAction.getInputTo()).isEqualTo("branch-to");

        verify(this.ghActionsKitMock).getRequiredInput("to");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetInputToNotProvided_thenFail() {
        when(this.ghActionsKitMock.getRequiredInput("to")).thenThrow(NoSuchElementException.class);
        assertThrows(CompletionException.class, () -> this.githubAction.execute());
        verify(this.ghActionsKitMock).getRequiredInput("to");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetInputFromProvided_thenReturnValue() throws Exception {
        when(this.ghActionsKitMock.getInput("from")).thenReturn(Optional.of("branch-name"));

        assertThat(this.githubAction.getInputFrom()).isEqualTo("branch-name");

        verify(this.ghActionsKitMock).getInput("from");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetInputFromNotProvided_thenReturnDefaultValue() throws Exception {
        when(this.ghActionsKitMock.getInput("from")).thenReturn(Optional.empty());
        when(this.ghActionsKitMock.getGitHubSha()).thenReturn("123456");

        assertThat(this.githubAction.getInputFrom()).isEqualTo("123456");

        verify(this.ghActionsKitMock).getInput("from");
        verify(this.ghActionsKitMock).getGitHubSha();
    }

    /**
     * Test method.
     */
    @Test
    void whenGetInputMessagePresent_thenReturnValue() throws Exception {
        when(this.ghActionsKitMock.getInput("message")).thenReturn(Optional.of("some message"));

        assertThat(this.githubAction.getInputMessage()).isEqualTo(Optional.of("some message"));

        verify(this.ghActionsKitMock).getInput("message");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetInputMessageEmpty_thenReturnEmpty() throws Exception {
        when(this.ghActionsKitMock.getInput("message")).thenReturn(Optional.empty());

        assertThat(this.githubAction.getInputMessage()).isEmpty();

        verify(this.ghActionsKitMock).getInput("message");
    }

    /**
     * Test method.
     */
    @Test
    void whenConnectApi_thenVerifyOK() throws Exception {
        when(ghActionsKitMock.getRequiredEnv("GITHUB_TOKEN")).thenReturn("token");
        when(ghActionsKitMock.getGitHubApiUrl()).thenReturn("https://api.github.com");

        this.githubAction.connectApi();

        verify(ghActionsKitMock).getRequiredEnv("GITHUB_TOKEN");
        verify(ghActionsKitMock).getGitHubApiUrl();
        verify(ghActionsKitMock, times(2)).debug(Mockito.anyString());
        verify(ghApiMock).checkApiUrlValidity();
    }

    /**
     * Test method.
     */
    @Test
    void whenGetTargetBranchExist_thenReturnBranch() throws Exception {
        var ghBranchMock = mock(GHBranch.class);
        when(ghRepositoryMock.getBranch("branch-name")).thenReturn(ghBranchMock);

        assertThat(this.githubAction.getToBranch("branch-name")).isPresent().contains(ghBranchMock);

        verify(ghRepositoryMock).getBranch("branch-name");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetTargetBranchDoesNotExist_thenReturnEmpty() throws Exception {
        when(ghRepositoryMock.getBranch("branch-name")).thenReturn(null);

        assertThat(this.githubAction.getToBranch("branch-name")).isEmpty();

        verify(ghRepositoryMock).getBranch("branch-name");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetTargetBranchNull_thenThrowNullPointerException() throws Exception {
        assertThrows(NullPointerException.class, () -> this.githubAction.getToBranch(null));
    }

    /**
     * Test method.
     */
    @Test
    void whenExecuteWithCommitToMerge_thenMergeCommitCreated() throws Exception {
        var spy = spy(this.githubAction);

        var ghRefFrom = mock(GHRef.class);
        when(ghRefFrom.getRef()).thenReturn("refs/heads/branch-from");

        var ghBranchTo = mock(GHBranch.class);

        var ghMergeCommit = mock(GHCommit.class);
        when(ghMergeCommit.getSHA1()).thenReturn("123456");

        when(this.ghActionsKitMock.getGitHubRepository()).thenReturn("octocat/Hello-World");
        doReturn("branch-from").when(spy).getInputFrom();
        doReturn("branch-to").when(spy).getInputTo();
        doReturn(Optional.of("some message")).when(spy).getInputMessage();

        doNothing().when(spy).connectApi();

        when(this.ghApiMock.getRepository("octocat/Hello-World")).thenReturn(ghRepositoryMock);
        doReturn(Optional.of(ghRefFrom)).when(spy).getAnyGHRef("branch-from");
        doReturn(Optional.of(ghBranchTo)).when(spy).getToBranch("branch-to");
        when(ghBranchTo.merge("refs/heads/branch-from", "some message")).thenReturn(ghMergeCommit);

        spy.execute();

        verify(this.ghActionsKitMock).getGitHubRepository();

        verify(spy).getInputFrom();
        verify(spy).getInputTo();
        verify(spy).getInputMessage();
        verify(spy).connectApi();
        verify(spy).getToBranch("branch-to");
        verify(spy).getAnyGHRef("branch-from");
        verify(ghBranchTo).merge("refs/heads/branch-from", "some message");

        verify(this.ghApiMock).getRepository("octocat/Hello-World");
        verify(this.ghActionsKitMock).notice(anyString());
        verify(this.ghActionsKitMock).setOutput(OutputVars.SHA.key(), "123456");
    }

    /**
     * Test method.
     */
    @Test
    void whenExecuteWithNoCommitToMerge_thenDoNothing() throws Exception {
        var spy = spy(this.githubAction);

        var ghRefFrom = mock(GHRef.class);
        when(ghRefFrom.getRef()).thenReturn("refs/heads/branch-from");

        var ghBranchTo = mock(GHBranch.class);
        when(ghBranchTo.getSHA1()).thenReturn("456789");

        when(this.ghActionsKitMock.getGitHubRepository()).thenReturn("octocat/Hello-World");
        doReturn("branch-from").when(spy).getInputFrom();
        doReturn("branch-to").when(spy).getInputTo();
        doReturn(Optional.of("some message")).when(spy).getInputMessage();

        doNothing().when(spy).connectApi();

        when(this.ghApiMock.getRepository("octocat/Hello-World")).thenReturn(ghRepositoryMock);
        doReturn(Optional.of(ghRefFrom)).when(spy).getAnyGHRef("branch-from");
        doReturn(Optional.of(ghBranchTo)).when(spy).getToBranch("branch-to");
        when(ghBranchTo.merge("refs/heads/branch-from", "some message")).thenReturn(null);

        spy.execute();

        verify(this.ghActionsKitMock).getGitHubRepository();

        verify(spy).getInputFrom();
        verify(spy).getInputTo();
        verify(spy).getInputMessage();
        verify(spy).connectApi();
        verify(spy).getToBranch("branch-to");
        verify(spy).getAnyGHRef("branch-from");
        verify(ghBranchTo).merge("refs/heads/branch-from", "some message");

        verify(this.ghApiMock).getRepository("octocat/Hello-World");
        verify(this.ghActionsKitMock).notice(anyString());
        verify(this.ghActionsKitMock).setOutput(OutputVars.SHA.key(), "456789");
    }

    /**
     * Test method.
     */
    @Test
    void whenExecuteWithFromBranchEmpty_thenThrowCompletionException() throws Exception {
        var spy = spy(this.githubAction);

        when(this.ghActionsKitMock.getGitHubRepository()).thenReturn("octocat/Hello-World");
        doReturn("branch-from").when(spy).getInputFrom();
        doReturn("branch-to").when(spy).getInputTo();
        doReturn(Optional.of("some message")).when(spy).getInputMessage();

        doNothing().when(spy).connectApi();

        when(this.ghApiMock.getRepository("octocat/Hello-World")).thenReturn(ghRepositoryMock);
        doReturn(Optional.empty()).when(spy).getAnyGHRef("branch-from");

        assertThrows(CompletionException.class, () -> spy.execute());

        verify(this.ghActionsKitMock).getGitHubRepository();

        verify(spy).getInputFrom();
        verify(spy).getInputTo();
        verify(spy).getInputMessage();
        verify(spy).connectApi();
        verify(spy).getAnyGHRef("branch-from");

        verify(this.ghApiMock).getRepository("octocat/Hello-World");
    }

    /**
     * Test method.
     */
    @Test
    void whenExecuteWithToBranchEmpty_thenThrowCompletionException() throws Exception {
        var spy = spy(this.githubAction);

        var ghRefFrom = mock(GHRef.class);

        when(this.ghActionsKitMock.getGitHubRepository()).thenReturn("octocat/Hello-World");
        doReturn("branch-from").when(spy).getInputFrom();
        doReturn("branch-to").when(spy).getInputTo();
        doReturn(Optional.of("some message")).when(spy).getInputMessage();

        doNothing().when(spy).connectApi();

        when(this.ghApiMock.getRepository("octocat/Hello-World")).thenReturn(ghRepositoryMock);
        doReturn(Optional.of(ghRefFrom)).when(spy).getAnyGHRef("branch-from");
        doReturn(Optional.empty()).when(spy).getToBranch("branch-to");

        assertThrows(CompletionException.class, () -> spy.execute());

        verify(this.ghActionsKitMock).getGitHubRepository();

        verify(spy).getInputFrom();
        verify(spy).getInputTo();
        verify(spy).getInputMessage();
        verify(spy).connectApi();
        verify(spy).getToBranch("branch-to");
        verify(spy).getAnyGHRef("branch-from");

        verify(this.ghApiMock).getRepository("octocat/Hello-World");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetAnyGHRefExist_thenReturnRef() throws Exception {
        var ghRef1 = Mockito.mock(GHRef.class);
        when(ghRef1.getRef()).thenReturn("refs/heads/main");

        var ghRef2 = Mockito.mock(GHRef.class);
        when(ghRef2.getRef()).thenReturn("refs/heads/BRANCH-name");

        var ghRef3 = Mockito.mock(GHRef.class);
        when(ghRef3.getRef()).thenReturn("refs/tags/1.0.0");

        var ghRef4 = Mockito.mock(GHRef.class);
        when(ghRef4.getRef()).thenReturn("refs/commits/123456");

        when(ghRepositoryMock.getRefs()).thenReturn(new GHRef[] {ghRef1, ghRef2, ghRef3, ghRef4});

        assertThat(this.githubAction.getAnyGHRef("refs/heads/main")).isPresent().contains(ghRef1);
        assertThat(this.githubAction.getAnyGHRef("branch-name")).isPresent().contains(ghRef2);
        assertThat(this.githubAction.getAnyGHRef("1.0.0")).isPresent().contains(ghRef3);
        assertThat(this.githubAction.getAnyGHRef("refs/tags/1.0.0")).isPresent().contains(ghRef3);
        assertThat(this.githubAction.getAnyGHRef("refs/commits/123456"))
                .isPresent()
                .contains(ghRef4);
        assertThat(this.githubAction.getAnyGHRef("123456")).isPresent().contains(ghRef4);

        verify(ghRepositoryMock, times(6)).getRefs();
        verify(ghRef1, times(6)).getRef();
        verify(ghRef2, times(5)).getRef();
        verify(ghRef3, times(4)).getRef();
        verify(ghRef4, times(2)).getRef();
    }

    /**
     * Test method.
     */
    @Test
    void whenGetAnyGHRefDoesNotExist_thenReturnEmpty() throws Exception {
        when(ghRepositoryMock.getRefs()).thenReturn(new GHRef[] {});

        assertThat(this.githubAction.getAnyGHRef("branch-name")).isEmpty();

        verify(ghRepositoryMock).getRefs();
    }

    /**
     * Test method.
     */
    @Test
    void whenGetAnyGHRefNull_thenThrowNullPointerException() throws Exception {
        assertThrows(NullPointerException.class, () -> this.githubAction.getAnyGHRef(null));
    }

    /**
     * Test method.
     */
    @Test
    void whenBranchRefNamePresent_thenReturnRefValue() {
        assertThat(this.githubAction.branchRef("branch-name")).isEqualTo("refs/heads/branch-name");
    }

    /**
     * Test method.
     */
    @Test
    void whenBranchRefNameNull_thenThrowNullPointerException() throws Exception {
        assertThrows(NullPointerException.class, () -> this.githubAction.branchRef(null));
    }

    /**
     * Test method.
     */
    @Test
    void whenTagRefNamePresent_thenReturnRefValue() {
        assertThat(this.githubAction.tagRef("1.0.0")).isEqualTo("refs/tags/1.0.0");
    }

    /**
     * Test method.
     */
    @Test
    void whenTagRefNameNull_thenThrowNullPointerException() throws Exception {
        assertThrows(NullPointerException.class, () -> this.githubAction.tagRef(null));
    }

    /**
     * Test method.
     */
    @Test
    void whenCommitRefNamePresent_thenReturnRefValue() {
        assertThat(this.githubAction.commitRef("123456")).isEqualTo("refs/commits/123456");
    }

    /**
     * Test method.
     */
    @Test
    void whenCommitRefNameNull_thenThrowNullPointerException() throws Exception {
        assertThrows(NullPointerException.class, () -> this.githubAction.commitRef(null));
    }
}
