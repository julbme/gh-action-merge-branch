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

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHRef;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import me.julb.sdk.github.actions.kit.GitHubActionsKit;
import me.julb.sdk.github.actions.spi.GitHubActionProvider;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;

/**
 * The action to merge branches. <br>
 * @author Julb.
 */
public class MergeBranchGitHubAction implements GitHubActionProvider {

    /**
     * The GitHub action kit.
     */
    @Setter(AccessLevel.PACKAGE)
    private GitHubActionsKit ghActionsKit = GitHubActionsKit.INSTANCE;

    /**
     * The GitHub API.
     */
    @Setter(AccessLevel.PACKAGE)
    private GitHub ghApi;

    /**
     * The GitHub repository.
     */
    @Setter(AccessLevel.PACKAGE)
    private GHRepository ghRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() {
        try {
            // Get inputs
            var from = getInputFrom();
            var to = getInputTo();
            var message = getInputMessage();

            // Trace parameters
            ghActionsKit.debug(
                    String.format("parameters: [from: %s, to: %s, message: %s]", from, to, message.orElse("")));

            // Read GitHub repository.
            connectApi();

            // Retrieve repository
            ghRepository = ghApi.getRepository(ghActionsKit.getGitHubRepository());

            // Get source ref
            var fromGHRef = getAnyGHRef(from).orElseThrow();

            // Get target branch
            var toGhBranch = getToBranch(to).orElseThrow();

            // Do the merge
            var ghMergeCommit = toGhBranch.merge(fromGHRef.getRef(), message.orElse(null));

            // Output vars.
            if (ghMergeCommit != null) {
                ghActionsKit.notice("Branch merged successfully.");
                ghActionsKit.setOutput(OutputVars.SHA.key(), ghMergeCommit.getSHA1());
            } else {
                ghActionsKit.notice("Nothing to merge.");
                ghActionsKit.setOutput(OutputVars.SHA.key(), toGhBranch.getSHA1());
            }
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    }

    // ------------------------------------------ Utility methods.

    /**
     * Gets the "from" input.
     * @return the "from" input.
     */
    String getInputFrom() {
        return ghActionsKit.getInput("from").orElse(ghActionsKit.getGitHubSha());
    }

    /**
     * Gets the "to" input.
     * @return the "to" input.
     */
    String getInputTo() {
        return ghActionsKit.getRequiredInput("to");
    }

    /**
     * Gets the "message" input.
     * @return the "v" input.
     */
    Optional<String> getInputMessage() {
        return ghActionsKit.getInput("message");
    }

    /**
     * Connects to GitHub API.
     * @throws IOException if an error occurs.
     */
    void connectApi() throws IOException {
        ghActionsKit.debug("github api url connection: check.");

        // Get token
        var githubToken = ghActionsKit.getRequiredEnv("GITHUB_TOKEN");

        // @formatter:off
        ghApi = Optional.ofNullable(ghApi)
                .orElse(new GitHubBuilder()
                        .withEndpoint(ghActionsKit.getGitHubApiUrl())
                        .withOAuthToken(githubToken)
                        .build());
        ghApi.checkApiUrlValidity();
        ghActionsKit.debug("github api url connection: ok.");
        // @formatter:on
    }

    /**
     * Gets the branch matching the given name.
     * @param name the branch name to look for.
     * @return the {@link GHBranch} for the given branch if exists, <code>false</code> otherwise.
     * @throws IOException if an error occurs.
     */
    Optional<GHBranch> getToBranch(@NonNull String name) throws IOException {
        return Optional.ofNullable(ghRepository.getBranch(name));
    }

    /**
     * Gets the {@link GHRef} branch or tag matching the given name.
     * @param name the branch or tag name to look for.
     * @return the {@link GHRef} for the given branch or tag if exists, <code>false</code> otherwise.
     * @throws IOException if an error occurs.
     */
    Optional<GHRef> getAnyGHRef(@NonNull String name) throws IOException {
        // Convert name to branch ref
        var branchRef = branchRef(name);

        // Convert name to tag ref
        var tagRef = tagRef(name);

        // Convert name to commit ref
        var commitRef = commitRef(name);

        // List of candidates for which ref is OK.
        var candidates =
                List.of(branchRef.toLowerCase(), tagRef.toLowerCase(), commitRef.toLowerCase(), name.toLowerCase());

        // Browse existing refs
        for (GHRef ghRef : ghRepository.getRefs()) {
            // Check if the ref is in the candidates.
            if (candidates.contains(ghRef.getRef().toLowerCase())) {
                return Optional.of(ghRef);
            }
        }

        return Optional.empty();
    }

    /**
     * Gets the ref from a branch name.
     * @param branchName the branch name.
     * @return the ref for the given branch name.
     */
    String branchRef(@NonNull String branchName) {
        return String.format("refs/heads/%s", branchName);
    }

    /**
     * Gets the ref from a tag name.
     * @param name the tag name.
     * @return the ref for the given tag name.
     */
    String tagRef(@NonNull String name) {
        return String.format("refs/tags/%s", name);
    }

    /**
     * Gets the ref from a commit sha.
     * @param name the commit sha.
     * @return the ref for the given commit.
     */
    String commitRef(@NonNull String name) {
        return String.format("refs/commits/%s", name);
    }
}
