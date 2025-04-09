import InboxIcon from "./Inbox.svg";

export default function DropZone({ onFiles }) {
  function selectFileToUpload() {
    const input = document.createElement("input");
    input.setAttribute("type", "file");
    input.setAttribute("accept", ".xml, .bpmn, .dmn");
    input.setAttribute("multiple", "true");

    input.addEventListener("change", () => {
      onFiles(input.files);
    });

    input.click();
  }

  function processFile(evt) {
    evt.preventDefault();

    const { files } = evt.dataTransfer;
    if (files) {
      onFiles(files);
    }
  }

  return (
    <div
      className="DropZone"
      onDragOver={(evt) => evt.preventDefault()}
      onDrop={processFile}
      onClick={selectFileToUpload}
    >
      <img src={InboxIcon} />
      <h2>Click or drag file to this area to upload</h2>
      <p>Upload .xml .bpmn and .dmn files. </p>
    </div>
  );
}
