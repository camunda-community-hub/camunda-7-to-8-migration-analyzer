import { useState, useEffect } from "react";

import {
  ProgressIndicator,
  ProgressStep,
  Button,
  Callout,
} from "@carbon/react";

import { Download, Launch } from "@carbon/react/icons";
import DropZone from "./DropZone";
import FileItem from "./FileItem";
import BpmnJS from 'bpmn-js';

function App() {
  const [step, setStep] = useState(0);
  const [files, setFiles] = useState([]);
  const [fileResults, setFileResults] = useState([]);
  const [validFiles, setValidFiles] = useState([]);
  const isSaaS = window.location.hostname !== "localhost";

  const [isPreviewOpen, setIsPreviewOpen] = useState(false);
  const [previewbpmnXml, setPreviewbpmnXml] = useState("");
  const [previewCheckJson, setPreviewCheckJson] = useState([]);

  useEffect(() => {
      if (isPreviewOpen && previewbpmnXml) {
          const viewer = new BpmnJS({ container: '#bpmnDiagram' });
          viewer.importXML(previewbpmnXml).then(() => {
            const canvas = viewer.get('canvas');
            canvas.zoom('fit-viewport');

            const elementsWithMessages =
              previewCheckJson?.[0]?.results?.filter((el) => el.messages?.length > 0) || [];

            elementsWithMessages.forEach((el) => {
              if (el.elementId) {
                  const severity = getMostSevere(el.messages);
                  if (severity) {
                    canvas.addMarker(el.elementId, `highlight-${severity.toLowerCase()}`);
                  }          
              }
            });

          });

      }
    }, [isPreviewOpen, previewbpmnXml]);

    function getMostSevere(messages) {
      const severityOrder = ['WARNING', 'TASK', 'REVIEW', 'INFO']; 
    
      let mostSevere = 'INFO';
    
      for (const msg of messages) {
        if (
          severityOrder.indexOf(msg.severity) >
          severityOrder.indexOf(mostSevere)
        ) {
          mostSevere = msg.severity;
        }
      }
    
      return mostSevere;
    }
    
  async function analyzeAndConvert() {
    setStep(2);
    setFileResults(files.map(() => ({ status: "uploading" })));    

    const uploadResults = await Promise.all(
      files.map(async (file, idx) => {
        const formData = new FormData();
        formData.append("file", file);

        const originalModelXml = await file.text();
        const checkResponse = await fetch("http://localhost:8080/check", {
          body: formData,
          method: "POST",
          headers: {
             "Accept": "application/json" 
          },
        });
        const checkResponseJson = await checkResponse.json();

        let result = {
          status: "uploading",
          originalModelXml: originalModelXml,
          checkResponseJson: checkResponseJson,
        };
        setFileResults((prevResults) => {
          const updated = [...prevResults];
          updated[idx] = result;
          return updated;
        });

        const convertResponse = await fetch("http://localhost:8080/convert", {
          body: formData,
          method: "POST",
        });

        // Extract filename from the Content-Disposition header
        let filename = "downloaded-model"; // Default filename

        const contentDisposition = convertResponse.headers.get("Content-Disposition");
        if (contentDisposition) {
          const match = contentDisposition.match(
              /filename\*?=(?:UTF-8'')?["']?([^"';]*)["']?/i
          );
          if (match) {
            filename = decodeURIComponent(match[1]); // Decode if necessary
          }
        }

        // Convert response to blob
        const blob = await convertResponse.blob();        

        result = {
          status: checkResponse.ok && convertResponse.ok ? "success" : "error",
          originalModelXml: originalModelXml,
          checkResponseJson: checkResponseJson,
          convertedFileBlob: blob, 
          filename
        };

        setFileResults((prevResults) => {
          const updated = [...prevResults];
          updated[idx] = result;
          return updated;
        });
        return result;
      })
    );

    const validFiles = files.filter(
      (_, idx) => uploadResults[idx].status === "success"
    );
    setValidFiles(validFiles);
  }

  async function downloadXLS() {
    const formData = new FormData();
    validFiles.forEach((file) => formData.append("file", file));
    await download(
      await fetch("/check", {
        body: formData,
        method: "POST",
        headers: {
          Accept:
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        },
      })
    );
  }
  async function downloadCSV() {
    const formData = new FormData();
    validFiles.forEach((file) => formData.append("file", file));
    await download(
      await fetch("/check", {
        body: formData,
        method: "POST",
        headers: {
          Accept: "text/csv",
        },
      })
    );
  }
  async function downloadZIP() {
    const formData = new FormData();
    validFiles.forEach((file) => formData.append("file", file));
    await download(
      await fetch("/convertBatch", {
        body: formData,
        method: "POST",
      })
    );
  }

  async function preview(response) {
    if (!response?.checkResponseJson) return;

    setPreviewCheckJson(response.checkResponseJson);
    setPreviewbpmnXml(response.originalModelXml);

    setIsPreviewOpen(true);
  }

  async function download(response) {
    let filename = response.filename;
    let blob = response.convertedFileBlob;

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
              Understand your BPMN and DMN models before migrating from Camunda
              7 to Camunda 8. Identify gaps, assess effort, and convert
              compatible elements.
            </p>
            {isSaaS && (
              <div>
                <p>
                  If you prefer a local version of this tool{" "}
                  <a href="https://github.com/camunda-community-hub/camunda-7-to-8-migration-analyzer?tab=readme-ov-file#installation">
                    download it here
                  </a>
                  .
                </p>
                <p>
                  <a href="https://legal.camunda.com/licensing-and-other-legal-terms#trial-and-free">
                    Check our legal terms and data privacy information.
                  </a>
                </p>
              </div>
            )}
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
              label="Analyze Results"
            />
          </ProgressIndicator>
        </div>


        {step === 0 && (
          <>
            <section>
              <h4>Instructions:</h4>
              <p>
                Upload your BPMN and DMN models. You can upload one or more
                files at once.
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
              Then go ahead and click the button below to analyze and convert
              your models.
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

        {step === 2 && (
          <>
            {/*
            <section>
              <Callout
                kind="success"
                title="Analysis and convertion complete"
                lowContrast
              />
            </section>
            */}
           
            <section>
              <h3>Your Models</h3>
              <p>
                Download models converted to Camunda 8 individually or as one Zip
                file. You can also preview the analysis result on the BPMN model.
              </p>
              {files.map((file, idx) => (
                <FileItem
                  key={file.name + "-" + idx}
                  name={file.name}
                  status={fileResults[idx].status}
                  isChecked={ fileResults[idx].checkResponseJson != null }
                  isConverted={fileResults[idx].convertedFileBlob != null}
                  previewAction={() => preview(fileResults[idx])}
                  downloadAction={() => download(fileResults[idx])}
                  error={
                    !fileResults[idx].ok == "error" ? "File upload failure" : ""
                  }
                />
              ))}
              <Button
                kind="tertiary"
                size="lg"
                renderIcon={Download}
                onClick={downloadZIP}
                disabled={validFiles.length === 0}
              >
                Download all successfully converted models as ZIP
              </Button>
            </section>
            <hr />

            <section>
              <h3>Analysis results</h3>
              <p>Download the  for all successfully converted models:</p>
              <div className="download-options">
                <div className="download-row">
                  <Button
                    kind="primary"
                    size="md"
                    renderIcon={Download}
                    onClick={downloadXLS}
                    className="withMarginBottom"
                    disabled={validFiles.length === 0}
                  >
                    Download XLSX
                  </Button>
                  <p>
                    Microsoft Excel file (XSLX) containing results and prepared
                    analysis.
                  </p>
                </div>
                <div className="download-row">
                  <Button
                    kind="primary"
                    size="md"
                    renderIcon={Download}
                    onClick={downloadCSV}
                    disabled={validFiles.length === 0}
                  >
                    Download CSV
                  </Button>
                  <p>
                    Comma Separated Values (CSV) file containing plain results to
                    import into your favorite tooling.
                  </p>
                  </div>  
                </div>
              <p>
                For more information on the analysis results,{" "}
                <a href="https://docs.camunda.io/docs/guides/migrating-from-camunda-7/migration-tooling/#migration-analyzer">
                  see the documentation
                </a>
                .
              </p>
            </section>
            <hr />

            <h3>Next steps for your migration</h3>
            <section>
              <p>
                Disvover next steps for your migration from Camunda 7 to Camunda
                8 in the migration guide.
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

{isPreviewOpen && (    
  <div className="modal-backdrop">
    <div className="modal">
      <div className="modal-header">
        <div className="left">
        <h2>Analysis - Graphical preview</h2>
        </div>
        <div className="right">
          <button onClick={() => setIsPreviewOpen(false)}>Close</button>
        </div>
      </div>
     
      <div id="bpmnDiagram" className="diagram-container"></div>

      <h3>Detailed results</h3>
      <table className="analysis-table">
        <thead>
          <tr>
          <th>Element Type</th>
            <th>Element ID</th>
            <th>Element Name</th>
            <th>Severity</th>
            <th>Message</th>
            <th>Link</th>
          </tr>
        </thead>
        <tbody>
          {previewCheckJson[0]?.results.flatMap((element, elementIdx) =>
            element.messages.length > 0
              ? element.messages.map((message, msgIdx) => (
                  <tr key={`${elementIdx}-${msgIdx}`}>
                    <td>{element.elementType}</td>
                    <td>{element.elementId}</td>
                    <td>{element.elementName}</td>
                    <td>{message.severity}</td>
                    <td>{message.message}</td>
                    <td>
                      {message.link ? (
                        <a href={message.link} target="_blank" rel="noopener noreferrer">
                          Link
                        </a>
                      ) : (
                        "-"
                      )}
                    </td>
                  </tr>
                ))
              : []
          )}
        </tbody>
      </table>

    </div>
  </div>
)}

      </div>
    </div>


  );

}

export default App;
