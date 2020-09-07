/*******************************************************************************
 * Copyright (c) 2009, 2018 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package hudson.plugins.jacoco.git;

import com.jcraft.jsch.Session;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.transport.OpenSshConfig.Host;

import java.io.File;
import java.io.IOException;


public class GitClone {

    /**
     * @param gitPath
     * @param tag
     * @param directory
     * @throws IOException
     * @throws GitAPIException
     */
    public static void cloneFiles(final String gitPath, final String tag, final String directory) throws IOException, GitAPIException {

        // 重写configure，设置"StrictHostKeyChecking","no"
        final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(final Host host, final Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }
        };

        // 创建仓库目录
        final File tagPathFile = new File(directory);
        UsernamePasswordCredentialsProvider user = new UsernamePasswordCredentialsProvider("username", "password");
        if (!tagPathFile.exists()) {// 如果目录不存在，就clone代码
            // 设置cloneCommand使用SSH的公钥认证
            final CloneCommand cloneCommand = Git.cloneRepository();

            cloneCommand.setCredentialsProvider(user);
            // 克隆仓库
            cloneCommand.setBranch(tag).setURI(gitPath)
                    .setDirectory(tagPathFile).call();
        }else {
            //如果目录存在，就拉最新代码
             Git git = new Git(new FileRepository(directory+"/.git"));
            git.pull().setRemoteBranchName(tag).
                    setCredentialsProvider(user).call();
        }

    }

}
