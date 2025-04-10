import { useState } from "react";

import {
  ProgressIndicator,
  ProgressStep,
  Button,
  Callout,
} from "@carbon/react";

import { Download, Launch } from "@carbon/react/icons";
import DropZone from "./DropZone";
import FileItem from "./FileItem";

function App() {
  const [step, setStep] = useState(0);
  const [files, setFiles] = useState([]);
  const [xlsTemplate, setXlsTemplate] = useState();
  const [csvTemplate, setCsvTemplate] = useState();
  const [fileResults, setFileResults] = useState();
  const [zip, setZip] = useState();
  const [hasValidFiles, setHasValidFiles] = useState(true);
  const isSaaS = window.location.hostname !== "localhost";

  async function analyzeAndConvert() {
    setStep(1);

    const fileResults = await Promise.all(
      files.map(async (file) => {
        const formData = new FormData();
        formData.append("file", file);

        return await fetch("/convert", {
          body: formData,
          method: "POST",
        });
      })
    );

    const validFiles = files.filter(
      (_, idx) => fileResults[idx].status === 200
    );
    setHasValidFiles(validFiles.length > 0);

    const formData = new FormData();
    validFiles.forEach((file) => formData.append("file", file));

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

    // get XLS template
    setCsvTemplate(
      await fetch("/check", {
        body: formData,
        method: "POST",
        headers: {
          Accept: "text/csv",
        },
      })
    );

    // get individual file results
    setFileResults(fileResults);

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
            <h2>Camunda Migration Analyzer</h2>
            <p>
              Understand your BPMN and DMN models before migrating from Camunda 7 to Camunda 8.
              Identify gaps, assess effort, and convert compatible elements.
            </p>
            {isSaaS && <div>
              <p>
                If you prefer a local version of this tool {" "}
                <a href="https://github.com/camunda-community-hub/camunda-7-to-8-migration-analyzer?tab=readme-ov-file#installation">download it here</a>.
              </p>
              <p>
                <a href="https://legal.camunda.com/licensing-and-other-legal-terms#trial-and-free">
                  Check our legal terms and data privacy information.
                </a>
              </p>
              </div>}
          </div>
        </div>
      </div>
      <div className="whiteBox centered">
        <div className="progressindicators">
          <ProgressIndicator spaceEqually>
            <ProgressStep
              current={step < 2}
              complete={step > 1}
              label="Upload Models"
            />
            <ProgressStep
              current={step === 2}
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
                Upload your BPMN and DMN models. You can upload one or more files at once. 
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
            <p>
                Then go ahead and click the button below to analyze and convert your models.
            </p>
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
              Upload your BPMN and DMN models. You can upload one or more files at once. 
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
            <p>
                Then go ahead and click the button below to analyze and convert your models.
            </p>
            <div className="analyzeButton">
              <Button kind="primary" size="lg" disabled>
                Analyze and convert
              </Button>
            </div>
          </>
        )}

        {step === 2 && (
          <>
            <section>
              <Callout
                kind="success"
                title="Analysis and convertion complete"
                lowContrast
              />
            </section>
            <section>
              <h3>Analysis results</h3>
              <p>
                Download the completed analysis:
              </p>
              <Button
                kind="primary"
                size="md"
                renderIcon={Download}
                onClick={() => download(xlsTemplate)}
                className="withMarginBottom"
                disabled={!hasValidFiles}
              >
                Download XLSX
              </Button>
              <p>
                Microsoft Excel file (XSLX) containing results and prepared analysis.
              </p>
              <Button
                kind="primary"
                size="md"
                renderIcon={Download}
                onClick={() => download(csvTemplate)}
                disabled={!hasValidFiles}
              >
                Download CSV
              </Button>
              <p>
                  Comma Separated Values (CSV) file containing plain results to import into your favorite tooling.
              </p>
              <p>
                For more information on the analysis results,{" "}
                <a href="https://docs.camunda.io/docs/guides/migrating-from-camunda-7/migration-tooling/#migration-analyzer">
                  see the documentation
                </a>.
              </p>
            </section>
            <hr />
            <section>
              <h3>Converted Models</h3>
              <p>
                Download the converted models below individually or as one Zip file:
              </p>
              {files.map((file, idx) => (
                <FileItem
                  key={file.name + "-" + idx}
                  name={file.name}
                  downloadAction={() => download(fileResults[idx])}
                  error={
                    fileResults[idx].status !== 200 ? "File upload failure" : ""
                  }
                />
              ))}
              <Button
                kind="tertiary"
                size="lg"
                renderIcon={Download}
                onClick={() => download(zip)}
                disabled={!hasValidFiles}
              >
                Download all converted models as ZIP
              </Button>
            </section>
            <hr />
            <h3>Next steps for your migration</h3>
            <section>
              <p>
                Disvover next steps for your migration from Camunda 7 to Camunda 8 in the migration guide.
              </p>
              <Button
                kind="tertiary"
                size="lg"
                renderIcon={Launch}
                href="https://docs.camunda.io/docs/guides/migrating-from-camunda-7/migration-journey/?utm_source=analyzer"
              >
                Migration Guide
              </Button>
            </section>            
          </>
        )}
      </div>
    </div>
  );
}

export default App;
