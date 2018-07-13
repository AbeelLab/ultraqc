try { npm install }
catch { "npm is not installed!
Please install it via https://nodejs.org/en/download/
" }

try { node app.js }
catch {
	"Node.js is not installed!
Please install it via https://nodejs.org/en/download/
"
}


Read-Host -Prompt "Press Enter to exit"

