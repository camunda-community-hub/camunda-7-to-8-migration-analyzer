import { useState } from "react";

import {
  FileUploaderItem,
  ProgressIndicator,
  ProgressStep,
  Button,
  Callout,
} from "@carbon/react";

import { Download, Launch, TrashCan } from "@carbon/react/icons";
import DropZone from "./DropZone";
import FileItem from "./FileItem";

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
      <div className="whiteBox">
        <div>
          <div>
            <h2>Analyze your Models</h2>
            <p>
              Understand your BPMN models before migrating. Identify gaps,
              assess effort, and convert compatible elements.
              <br />
              <br />
              If you would prefer to use a local version of this tool please{" "}
              <a href="https://google.com">download it here</a>
            </p>
            <p>
              <a href="https://legal.camunda.com/licensing-and-other-legal-terms#trial-and-free">
                See here for Data privacy information
              </a>
            </p>
          </div>
        </div>
      </div>
      <div className="whiteBox centered">
        <div className="progressindicators">
          <ProgressIndicator spaceEqually>
            <ProgressStep
              current={step === 0}
              complete={step > 0}
              label="Upload Models"
            />
            <ProgressStep
              current={step === 1 || step === 2}
              complete={step > 2}
              label="Analyze Models"
            />
          </ProgressIndicator>
        </div>

        {step === 0 && (
          <>
            <section>
              <h4>Instructions:</h4>
              <p>
                Upload your BPMN models. You can upload single Models or
                multiple once your models are uploded
                <br />
                go ahead and Analyze and convert.
              </p>
            </section>
            <div className="fileUploadBox">
              <DropZone
                onFiles={(files) => {
                  setFiles((prevFiles) => [...prevFiles, ...files]);
                }}
              />
              {files.map((file, idx) => (
                <FileItem
                  key={file.name + "-" + idx}
                  name={file.name}
                  status="edit"
                  onDelete={() => {
                    setFiles((prevFiles) =>
                      prevFiles.filter((prevFile) => prevFile !== file)
                    );
                  }}
                />
              ))}
            </div>

            <div className="analyzeButton">
              <Button
                kind="primary"
                size="lg"
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
              <h4>Instructions:</h4>
              <p>
                Upload your BPMN models. You can upload single Models or
                multiple once your models are uploded go ahead and Analyze and
                convert.
              </p>
            </section>
            <div className="fileUploadBox">
              <DropZone
                onFiles={() => {
                  // do nothing while upload is processing
                }}
              />
              {files.map((file, idx) => (
                <FileItem
                  key={file.name + "-" + idx}
                  name={file.name}
                  status="uploading"
                />
              ))}
            </div>

            <Button
              kind="primary"
              size="md"
              onClick={analyzeAndConvert}
              disabled
            >
              Analyze and convert
            </Button>
          </>
        )}

        {step === 2 && (
          <>
            <section>
              <Callout
                kind="success"
                title="Analysis complete"
                subtitle="View your analyzed Models"
                lowContrast
              />
            </section>
            <section>
              <h4>Analyze results</h4>
              <p>
                Access the completed analysis below. You can download view the
                results as a XLS sheet or a csv. <br />
                <a href="https://docs.camunda.io/docs/guides/migrating-from-camunda-7/migration-tooling/#migration-analyzer">
                  For more information see more about the Camunda Analyzer here
                </a>
              </p>
              <Button
                kind="primary"
                size="md"
                renderIcon={Download}
                onClick={() => download(xlsTemplate)}
              >
                Download analyzed results XLS
              </Button>
              <br />
              <Button
                kind="primary"
                size="md"
                renderIcon={Download}
                onClick={() => download(xlsTemplate)}
              >
                Download analyzed results CSV
              </Button>
            </section>

            <section>
              <h4>Converted Models</h4>
              <p>
                See below the converted models that you can download
                individually or as a Zip file
              </p>
              {files.map((file, idx) => (
                <div key={file.name + "-" + idx} className="individualDownload">
                  <span>{file.name}</span>
                  <button onClick={() => download(fileResults[idx])}>
                    <Download />
                  </button>
                  <button
                    onClick={() =>
                      setFiles((prevFiles) =>
                        prevFiles.filter((prevFile) => prevFile !== file)
                      )
                    }
                  >
                    <TrashCan />
                  </button>
                </div>
              ))}
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
            <h3>Next steps for your migration</h3>
            <section>
              <h4>Migration guide</h4>
              <p>
                Find all the information that you need for your migration needs
                in our guides.
              </p>
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
                See how to use AI to enhance your migration processes.
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
