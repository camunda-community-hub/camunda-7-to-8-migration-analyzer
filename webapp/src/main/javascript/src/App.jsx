import { useState } from "react";

import {
  FileUploaderDropContainer,
  FileUploaderItem,
  ProgressIndicator,
  ProgressStep,
  Button,
  Callout,
} from "@carbon/react";

import { Download, Launch } from "@carbon/react/icons";

function App() {
  const [step, setStep] = useState(0);
  const [files, setFiles] = useState([]);
  const [xlsTemplate, setXlsTemplate] = useState();
  const [fileResults, setFileResults] = useState();
  const [zip, setZip] = useState();

  async function analyzeAndConvert() {
    setStep(1);

    const formData = new FormData();
    files.forEach((file) => formData.append("file", file));

    // get XLS template
    setXlsTemplate(
      await fetch("/check", {
        body: formData,
        method: "POST",
        headers: {
          Accept:
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        },
      })
    );

    // get individual file results
    setFileResults(
      await Promise.all(
        files.map(async (file) => {
          const formData = new FormData();
          formData.append("file", file);

          return await fetch("/convert", {
            body: formData,
            method: "POST",
          });
        })
      )
    );

    // get ZIP file result
    setZip(
      await fetch("/convertBatch", {
        body: formData,
        method: "POST",
      })
    );

    setStep(2);
  }

  async function download(response) {
    // Extract filename from the Content-Disposition header
    const contentDisposition = response.headers.get("Content-Disposition");
    let filename = "downloaded-file"; // Default filename

    if (contentDisposition) {
      const match = contentDisposition.match(
        /filename\*?=(?:UTF-8'')?["']?([^"';]*)["']?/i
      );
      if (match) {
        filename = decodeURIComponent(match[1]); // Decode if necessary
      }
    }

    // Convert response to blob
    const blob = await response.blob();
    const url = URL.createObjectURL(blob);

    // Create and trigger download link
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);

    // Clean up the object URL
    URL.revokeObjectURL(url);
  }

  return (
    <div className="container">
      <h1>
        Seamlessly analyze your BPMN models. Get detailed insights and
        ready-to-use templates in just a few clicks
      </h1>
      <div className="whiteBox">
        <div>
          <div>
            <h2>This is some copy</h2>
            <p>
              {step === 0
                ? "This copy will talk about what Analyzing and converting mean for them as the first step of the migration process"
                : "Breaking down a lengthy or unfamiliar form task into multiple steps that the user is then guided through."}
            </p>
          </div>
        </div>
      </div>
      <div className="whiteBox">
        <ProgressIndicator spaceEqually>
          <ProgressStep
            current={step === 0}
            complete={step > 0}
            label="Upload your models"
            description="Step 1"
          />
          <ProgressStep
            current={step === 1 || step === 2}
            complete={step > 2}
            label="Analyze your models"
            description="Step 2"
          />
          <ProgressStep
            current={step === 3}
            label="Prepare for xxxxx"
            description="Step 3"
          />
        </ProgressIndicator>

        {step === 0 && (
          <>
            <div className="fileUploadBox">
              <FileUploaderDropContainer
                accept={[".bpmn", ".zip"]}
                multiple
                labelText="Click or drag file to this area to upload"
                onAddFiles={(evt) => {
                  setFiles((prevFiles) => [...prevFiles, ...evt.target.files]);
                }}
              />
              {files.map((file, idx) => (
                <FileUploaderItem
                  key={file.name + "-" + idx}
                  name={file.name}
                  status="edit"
                  size="sm"
                  onDelete={() => {
                    setFiles((prevFiles) =>
                      prevFiles.filter((prevFile) => prevFile !== file)
                    );
                  }}
                />
              ))}
            </div>
            <section>
              <h4>Instructions:</h4>
              <p>Upload your BPMN models.</p>
              <p>Here are some FAQ's about the product.</p>
              <p>Data privacy information for users</p>
            </section>
            <div>
              <Button
                kind="primary"
                size="md"
                onClick={analyzeAndConvert}
                disabled={files.length === 0}
              >
                Analyze and convert
              </Button>
            </div>
          </>
        )}

        {step === 1 && (
          <>
            <section>
              <Callout
                title="Analyzing"
                subtitle="Sit tight while we analyze your files."
                lowContrast
              />
            </section>
            <section>
              {files.map((file, idx) => (
                <FileUploaderItem
                  key={file.name + "-" + idx}
                  name={file.name}
                  status="uploading"
                  size="sm"
                />
              ))}
            </section>
            <hr />
            <section>
              <h4>Analyze results</h4>
              <p>
                Once your files are analyzed, you can download the template to
                see the results. You will see a breakdown of XXXXXXX
              </p>
            </section>
          </>
        )}

        {step === 2 && (
          <>
            <section>
              <Callout
                kind="success"
                title="Analysis complete"
                subtitle="See your analyzes files and take the relevant actions"
                lowContrast
              />
            </section>
            <section>
              <h4>Analyze results</h4>
              <p>
                Once your files are analyzed, you can download the template to
                see the results. You will see a breakdown of XXXXXXX
              </p>
              <Button
                kind="primary"
                size="md"
                renderIcon={Download}
                onClick={() => download(xlsTemplate)}
              >
                Download XLS template
              </Button>
            </section>

            <section>
              <h4>Converted Models</h4>
              <p>
                Here are the converted models that you can download individually
                or as a Zip file
              </p>
              <section>
                {files.map((file, idx) => (
                  <div
                    key={file.name + "-" + idx}
                    className="individualDownload"
                  >
                    <span>{file.name}</span>
                    <button onClick={() => download(fileResults[idx])}>
                      <Download />
                    </button>
                  </div>
                ))}
              </section>
              <Button
                kind="tertiary"
                size="md"
                renderIcon={Download}
                onClick={() => download(zip)}
              >
                Download all converted models as zip
              </Button>
            </section>
            <hr />
            <section>
              <Button
                kind="tertiary"
                size="md"
                renderIcon={Download}
                onClick={() => setStep(3)}
              >
                Be Prepared!
              </Button>
            </section>
          </>
        )}

        {step === 3 && (
          <>
            <section>
              <h4>Migration guide</h4>
              <p>Information about the guide and why it will help</p>
              <Button
                kind="tertiary"
                size="md"
                renderIcon={Launch}
                href="https://google.com"
              >
                Read migration guide
              </Button>
            </section>
            <section>
              <h4
                style={{
                  marginTop: "2rem",
                  textDecoration: "underline",
                  fontSize: "0.9rem",
                }}
              >
                AI Tutorial
              </h4>
              <p style={{ fontSize: "0.9rem" }}>
                Information about the AI tutorial and why it will help
              </p>
              <Button
                kind="tertiary"
                size="md"
                renderIcon={Launch}
                href="https://google.com"
              >
                Watch AI tutorial
              </Button>
            </section>
          </>
        )}
      </div>
    </div>
  );
}

export default App;
