<%--
  #%L
  webappRunnerSample Maven Webapp
  %%
  Copyright (C) 2016 - 2018 Frederik Kammel
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  --%>
<html>
<body>
<h1 id="itworks">It works!</h1><br>
<h1 id="tictactoe">Tic Tac Toe</h1>

<p>Good news! Your server is working and you can reach it through the following ip:</p>
<% StringBuffer url = request.getRequestURL();

    if (!url.toString().endsWith("/"))
        url.append("/");

    url.append("tictactoe");
    out.println(url);%>

<p>Read more on <a href="https://github.com/vatbub/tictactoe">GitHub</a>.</p>
</body>
</html>
