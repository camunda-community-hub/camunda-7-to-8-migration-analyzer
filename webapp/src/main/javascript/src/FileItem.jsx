import Paperclip from "./Paperclip.svg";
import Trash from "./Trash.svg";

export default function DropZone({
  name,
  error,
  status,
  downloadAction,
  onDelete,
}) {
  return (
    <div className="FileItem">
      <div className="left">
        <img src={Paperclip} />
        <span>{name}</span>
      </div>
      <div className="right">
        {onDelete && (
          <button onClick={onDelete}>
            <img src={Trash} />
          </button>
        )}
      </div>
    </div>
  );
}
